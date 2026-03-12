package com.jahnavi.contribution.contribution_management.service.impl;

import com.jahnavi.contribution.contribution_management.dto.BulkClassificationDecisionRequestDto;
import com.jahnavi.contribution.contribution_management.dto.BulkClassificationDecisionResponseDto;
import com.jahnavi.contribution.contribution_management.dto.BulkClassificationErrorDto;
import com.jahnavi.contribution.contribution_management.dto.ClassificationResponseDto;
import com.jahnavi.contribution.contribution_management.entity.RawCreditCombined;
import com.jahnavi.contribution.contribution_management.entity.VirtualAccount;
import com.jahnavi.contribution.contribution_management.entity.VirtualAccountTransaction;
import com.jahnavi.contribution.contribution_management.enums.Classification;
import com.jahnavi.contribution.contribution_management.enums.ClassificationStatus;
import com.jahnavi.contribution.contribution_management.repository.RawCreditCombinedRepository;
import com.jahnavi.contribution.contribution_management.repository.VirtualAccountRepository;
import com.jahnavi.contribution.contribution_management.repository.VirtualAccountTransactionRepository;
import com.jahnavi.contribution.contribution_management.service.ClassificationService;
import com.jahnavi.contribution.exception.CoreException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassificationServiceImpl implements ClassificationService {

    private final VirtualAccountTransactionRepository transactionRepository;
    private final RawCreditCombinedRepository rawCreditCombinedRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static final String[] BULK_UPLOAD_HEADERS = {
        "UTR","Classification","Amount","Date Time of Transaction",
        "Reason","Folio Number","Bank Account Number","IFSC","Payment Mode"
    };

    private static final String CLASSIFICATION_PROPER = "PROPER";

    @Override
    public List<ClassificationResponseDto> getClassificationListByFilter(
            Optional<Classification> classification,
            Optional<LocalDateTime> startDate,
            Optional<LocalDateTime> endDate,
            Optional<String> vaPrefix) {
        log.info("Fetching classification list with filters - classification: {}, startDate: {}, endDate: {}, vaPrefix: {}", 
                classification.orElse(null), startDate.orElse(null), endDate.orElse(null), vaPrefix.orElse(null));
        
        // Use the new repository method that handles all filters
        List<VirtualAccountTransaction> transactions = transactionRepository.findTransactionsWithFilters(
                classification.orElse(null),
                startDate.orElse(null),
                endDate.orElse(null),
                vaPrefix.orElse(null)
        );
        
        Map<String, VirtualAccount> vaMap = getVirtualAccountMap(transactions);
        
        return transactions.stream()
                .map(transaction -> mapToClassificationResponse(transaction, vaMap))
                .toList();
    }

    @Override
    public void downloadClassificationReport(HttpServletResponse response) throws IOException {
        log.info("Downloading classification report");
        
        List<VirtualAccountTransaction> transactions = transactionRepository.findAllWithFolioInfo();
        Map<String, VirtualAccount> vaMap = getVirtualAccountMap(transactions);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Classification Report");
            CellStyle headerStyle = createHeaderStyle(workbook);
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "UTR", "Classification", "Amount",
                "Date Time of Transaction", "Reason", "Folio Number", 
                "Bank Account Number", "IFSC", "Payment Mode"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            

            int rowNum = 1;
            for (VirtualAccountTransaction transaction : transactions) {
                Row row = sheet.createRow(rowNum++);
                VirtualAccount va = vaMap.get(transaction.getVirtualAccountNumber());
                
                int colNum = 0;
                row.createCell(colNum++).setCellValue(safeString(transaction.getUtr()));
                row.createCell(colNum++).setCellValue(safeString(transaction.getClassification() != null ? transaction.getClassification().name() : ""));
                row.createCell(colNum++).setCellValue(safeBigDecimal(transaction.getTransactionAmount()));
                row.createCell(colNum++).setCellValue(formatDateTime(transaction.getTransactionDate()));
                row.createCell(colNum++).setCellValue(safeString(transaction.getReason()));
                row.createCell(colNum++).setCellValue(va != null ? safeString(va.getFolioNumber()) : "");
                String bankAccount = transaction.getRemitterAccount();
                if (bankAccount == null || bankAccount.trim().isEmpty()) {
                    bankAccount = transaction.getClientAccount();
                }
                row.createCell(colNum++).setCellValue(safeString(bankAccount));
                row.createCell(colNum++).setCellValue(safeString(transaction.getRemitterIfsc()));
                row.createCell(colNum++).setCellValue(safeString(transaction.getTransactionType()));
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=classification_report.xlsx");
            
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
            
            log.info("Classification report downloaded successfully");
        } catch (Exception e) {
            log.error("Error generating classification report: {}", e.getMessage(), e);
            throw new CoreException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error generating classification report: " + e.getMessage());
        }
    }

    private Map<String, VirtualAccount> getVirtualAccountMap(List<VirtualAccountTransaction> transactions) {
        List<String> vaNumbers = transactions.stream()
                .map(VirtualAccountTransaction::getVirtualAccountNumber)
                .distinct()
                .toList();
        
        List<VirtualAccount> virtualAccounts = new ArrayList<>();
        for (String vaNumber : vaNumbers) {
            Optional<VirtualAccount> vaOpt = virtualAccountRepository.findByVaNumber(vaNumber);
            vaOpt.ifPresent(virtualAccounts::add);
        }
        
        return virtualAccounts.stream()
                .collect(Collectors.toMap(VirtualAccount::getVaNumber, va -> va));
    }

    private ClassificationResponseDto mapToClassificationResponse(
            VirtualAccountTransaction transaction, Map<String, VirtualAccount> vaMap) {
        VirtualAccount va = vaMap.get(transaction.getVirtualAccountNumber());
        log.debug("Mapping classification response for transaction id: {}", transaction.getId());
        return ClassificationResponseDto.builder()
                .utr(transaction.getUtr())
                .va(transaction.getVirtualAccountNumber())
                .folio(va != null ? va.getFolioNumber() : null)
                .amount(parseBigDecimal(transaction.getTransactionAmount()))
                .classification(transaction.getClassification() != null ? transaction.getClassification().name() : null)
                .reason(transaction.getReason())
                .bankAccount(transaction.getRemitterAccount())
                .ifsc(transaction.getRemitterIfsc())
                .paymentMode(transaction.getTransactionType())
                .dateAndTime(transaction.getTransactionDate())
                .build();
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        return headerStyle;
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("Unable to parse BigDecimal from value: {}", value);
            return null;
        }
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private String safeBigDecimal(String value) {
        BigDecimal bd = parseBigDecimal(value);
        return bd != null ? bd.toString() : "";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }

    public BulkClassificationDecisionResponseDto bulkUploadClassificationDecision(MultipartFile file) {

        log.info("Processing bulk classification decision upload");

        validateFile(file);

        List<BulkClassificationDecisionRequestDto> requestDtos = new ArrayList<>();
        List<BulkClassificationErrorDto> errors = new ArrayList<>();

        readExcelFile(file, requestDtos, errors);
        validateRows(requestDtos, errors);
        processValidRows(requestDtos);

        return buildSuccessResponse(requestDtos.size());
    }

    private void readExcelFile(MultipartFile file,
                               List<BulkClassificationDecisionRequestDto> requestDtos,
                               List<BulkClassificationErrorDto> errors) {

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new CoreException(HttpStatus.BAD_REQUEST.value(), "Excel file contains no sheets");
            }

            validateHeaders(sheet);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }
                parseRowSafely(row, i + 1, requestDtos, errors);
            }

        } catch (Exception e) {
            log.error("Error reading bulk upload file", e);
            throw new CoreException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Error reading file: " + e.getMessage()
            );
        }
    }

    private void parseRowSafely(Row row,
                                int rowNumber,
                                List<BulkClassificationDecisionRequestDto> requestDtos,
                                List<BulkClassificationErrorDto> errors) {

        try {
            requestDtos.add(parseRow(row));
        } catch (Exception e) {
            errors.add(BulkClassificationErrorDto.builder()
                    .rowNumber(rowNumber)
                    .utr(getCellValueAsString(row.getCell(0)))
                    .errorMessage("Error parsing row: " + e.getMessage())
                    .build());
        }
    }

    private void validateRows(List<BulkClassificationDecisionRequestDto> requestDtos,
                              List<BulkClassificationErrorDto> errors) {

        int rowNumber = 1;

        for (BulkClassificationDecisionRequestDto dto : requestDtos) {
            String error = validateRow(dto, rowNumber);
            if (error != null) {
                errors.add(BulkClassificationErrorDto.builder()
                        .rowNumber(rowNumber)
                        .utr(dto.getUtr())
                        .errorMessage(error)
                        .build());
            }
            rowNumber++;
        }

        if (!errors.isEmpty()) {
            throwValidationException(errors);
        }
    }

    private void throwValidationException(List<BulkClassificationErrorDto> errors) {

        List<BulkClassificationErrorDto> properErrors = errors.stream()
                .filter(e -> e.getErrorMessage() != null &&
                        e.getErrorMessage().contains("already PROPER"))
                .toList();

        if (!properErrors.isEmpty()) {
            BulkClassificationErrorDto first = properErrors.get(0);
            throw new CoreException(
                    HttpStatus.BAD_REQUEST.value(),
                    String.format(
                            "Bulk upload rejected: Found %d record(s) that are already PROPER. " +
                                    "First error at row %d, UTR: %s",
                            properErrors.size(),
                            first.getRowNumber(),
                            first.getUtr()
                    )
            );
        }

        BulkClassificationErrorDto first = errors.get(0);
        throw new CoreException(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed for " + errors.size() +
                        " row(s). First error at row " +
                        first.getRowNumber() + ": " +
                        first.getErrorMessage()
        );
    }

    private void processValidRows(List<BulkClassificationDecisionRequestDto> requestDtos) {
        for (BulkClassificationDecisionRequestDto dto : requestDtos) {
            try {
                processClassificationDecision(dto);
            } catch (Exception e) {
                log.error("Error processing UTR {}", dto.getUtr(), e);
                throw new CoreException(
                        HttpStatus.BAD_REQUEST.value(),
                        "Error processing UTR " + dto.getUtr() + ": " + e.getMessage()
                );
            }
        }
    }


    private BulkClassificationDecisionResponseDto buildSuccessResponse(int totalRecords) {

        return BulkClassificationDecisionResponseDto.builder()
                .totalRecords(totalRecords)
                .successCount(totalRecords)
                .failedCount(0)
                .message(String.format("Successfully processed %d record(s)", totalRecords))
                .errors(new ArrayList<>())
                .build();
    }


    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "File is empty");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "Only XLSX files are supported");
        }
    }

    private void validateHeaders(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "Header row is missing");
        }
        
        for (int i = 0; i < BULK_UPLOAD_HEADERS.length; i++) {
            Cell cell = headerRow.getCell(i);
            String cellValue = getCellValueAsString(cell);
            if (!BULK_UPLOAD_HEADERS[i].equalsIgnoreCase(cellValue.trim())) {
                throw new CoreException(HttpStatus.BAD_REQUEST.value(),
                        "Invalid header at column " + (i + 1) + ". Expected: " + BULK_UPLOAD_HEADERS[i] + 
                        ", Found: " + cellValue);
            }
        }
    }

    private BulkClassificationDecisionRequestDto parseRow(Row row) {
        BulkClassificationDecisionRequestDto dto = new BulkClassificationDecisionRequestDto();

        dto.setUtr(getCellValueAsString(row.getCell(0)));
        dto.setClassification(getCellValueAsString(row.getCell(1)));
        dto.setFolioNumber(getCellValueAsString(row.getCell(5)));
        dto.setIfsc(getCellValueAsString(row.getCell(7)));
        
        return dto;
    }
    private String validateRow(BulkClassificationDecisionRequestDto dto, int rowNumber) {

        String basicError = validateMandatoryFields(dto);
        if (basicError != null) {
            return basicError;
        }

        Classification classification = parseClassification(dto);
        findAndValidateTransaction(dto, rowNumber);
        if (classification == Classification.PROPER) {
            return validateProperFields(dto);
        }

        return null;
    }



    private String validateMandatoryFields(BulkClassificationDecisionRequestDto dto) {

        if (dto.getUtr() == null || dto.getUtr().trim().isEmpty()) {
            return "UTR is required";
        }

        if (dto.getClassification() == null || dto.getClassification().trim().isEmpty()) {
            return "Classification is required";
        }

        if (dto.getIfsc() == null || dto.getIfsc().trim().length() < 4) {
            return "IFSC must be provided with at least 4 characters";
        }

        return null;
    }
    private Classification parseClassification(BulkClassificationDecisionRequestDto dto) {

        return Classification.from(dto.getClassification())
                .orElseThrow(() ->
                        new CoreException(
                                HttpStatus.BAD_REQUEST.value(),
                                "Classification must be PROPER or IMPROPER"
                        )
                );
    }


    private void findAndValidateTransaction(
            BulkClassificationDecisionRequestDto dto,
            int rowNumber) {

        String ifscPrefix = dto.getIfsc().trim().substring(0, 4);

        VirtualAccountTransaction transaction = transactionRepository
                .findByUtrAndIfscPrefix(dto.getUtr().trim(), ifscPrefix)
                .orElseThrow(() ->
                        new CoreException(
                                HttpStatus.BAD_REQUEST.value(),
                                "Transaction not found for UTR: " + dto.getUtr()
                                        + " with IFSC prefix: " + ifscPrefix
                        )
                );

        if (transaction.getClassification() == Classification.PROPER) {
            throw new CoreException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Transaction is already PROPER. Cannot edit PROPER transactions. Row: " + rowNumber
            );
        }
    }

    private String validateProperFields(BulkClassificationDecisionRequestDto dto) {

        if (dto.getFolioNumber() == null || dto.getFolioNumber().trim().isEmpty()) {
            return "Folio number is mandatory when changing to PROPER";
        }

        String folio = dto.getFolioNumber().trim();

      return folio;

    }


    /**
     * Updates classification for the given row.
     */
    private void processClassificationDecision(BulkClassificationDecisionRequestDto dto) {
        String ifscPrefix = dto.getIfsc().trim().substring(0, 4);
        Optional<VirtualAccountTransaction> transactionOpt = transactionRepository
                .findByUtrAndIfscPrefix(dto.getUtr().trim(), ifscPrefix);

        if (transactionOpt.isEmpty()) {
            throw new CoreException(HttpStatus.NOT_FOUND.value(),
                    "Transaction not found for UTR: " + dto.getUtr());
        }

        VirtualAccountTransaction transaction = transactionOpt.get();

        String classification = dto.getClassification().trim().toUpperCase();
        if (CLASSIFICATION_PROPER.equals(classification)) {
            transaction.setClassification(Classification.PROPER);
            transaction.setClassificationStatus(ClassificationStatus.USER_APPROVED);
        } else {
            transaction.setClassification(Classification.IMPROPER);
        }

        transactionRepository.save(transaction);
        log.info("Updated classification for UTR {} to {} in VirtualAccountTransaction", dto.getUtr(), classification);

        Optional<RawCreditCombined> rawCreditOpt =
                rawCreditCombinedRepository
                        .findByUtrAndIfscPrefix(
                                dto.getUtr().trim(),
                                ifscPrefix
                        );

        if (rawCreditOpt.isPresent()) {
            RawCreditCombined rawCredit = rawCreditOpt.get();
            if (CLASSIFICATION_PROPER.equals(classification)) {
                rawCredit.setClassification(Classification.PROPER);
                rawCredit.setClassificationStatus(ClassificationStatus.USER_APPROVED);
            } else {
                rawCredit.setClassification(Classification.IMPROPER);
            }
            rawCreditCombinedRepository.save(rawCredit);
            log.info("Updated classification for UTR {} to {} in RawCreditCombined", dto.getUtr(), classification);
        } else {
            log.warn("RawCreditCombined record not found for UTR: {} with IFSC prefix: {}", dto.getUtr(), ifscPrefix);
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
