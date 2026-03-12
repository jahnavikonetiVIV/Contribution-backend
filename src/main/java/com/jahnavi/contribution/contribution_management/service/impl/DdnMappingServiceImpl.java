package com.jahnavi.contribution.contribution_management.service.impl;

import com.jahnavi.contribution.contribution_management.dto.AutoTagRequest;
import com.jahnavi.contribution.contribution_management.dto.AutoTagResponse;
import com.jahnavi.contribution.contribution_management.dto.AutoTagResultDto;
import com.jahnavi.contribution.contribution_management.dto.BulkDdnMappingResponse;
import com.jahnavi.contribution.contribution_management.dto.BulkDdnMappingUploadRow;
import com.jahnavi.contribution.contribution_management.dto.DdnMappingFilterRequest;
import com.jahnavi.contribution.contribution_management.dto.DdnMappingHistoryDto;
import com.jahnavi.contribution.contribution_management.dto.DdnMappingManualRequest;
import com.jahnavi.contribution.contribution_management.dto.DdnMappingResponse;
import com.jahnavi.contribution.contribution_management.dto.DdnOptionDto;
import com.jahnavi.contribution.contribution_management.dto.TransactionAmountRequest;
import com.jahnavi.contribution.contribution_management.dto.TransactionAmountResponse;
import com.jahnavi.contribution.contribution_management.entity.DdnContributionMapping;
import com.jahnavi.contribution.contribution_management.entity.VirtualAccount;
import com.jahnavi.contribution.contribution_management.entity.VirtualAccountTransaction;
import com.jahnavi.contribution.contribution_management.enums.Classification;
import com.jahnavi.contribution.contribution_management.enums.ClassificationStatus;
import com.jahnavi.contribution.contribution_management.repository.DdnContributionMappingRepository;
import com.jahnavi.contribution.contribution_management.repository.DdnMappingSpecification;
import com.jahnavi.contribution.contribution_management.repository.VirtualAccountRepository;
import com.jahnavi.contribution.contribution_management.repository.VirtualAccountTransactionRepository;
import com.jahnavi.contribution.contribution_management.service.DdnDataService;
import com.jahnavi.contribution.contribution_management.service.DdnMappingService;
import com.jahnavi.contribution.exception.CoreException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DdnMappingServiceImpl implements DdnMappingService {

    private static final String MAPPING_TYPE_AUTO = "AUTO";
    private static final String MAPPING_TYPE_MANUAL = "MANUAL";
    private static final String MAPPING_TYPE_BULK = "BULK";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_CLOSED = "CLOSED";
    private static final String TRANSACTION_SOURCE_MIS = "MIS";
    private static final String MAPPING_SOURCE_SYSTEM = "System";
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private static final String HEADER_UTR = "UTR";
    private static final String HEADER_IFSC = "IFSC";
    private static final String HEADER_TRANSACTION_AMOUNT = "Transaction Amount";
    private static final String HEADER_INITIAL_AMOUNT = "Initial Amount";
    private static final String HEADER_INITIAL_COMMITMENT_DDN_ID = "Initial Commitment DDN ID";
    private static final String HEADER_TOPUP_AMOUNT = "Topup Amount";
    private static final String HEADER_TOPUP_DDN_ID = "Topup DDN ID";
    private static final String HEADER_EXCESS_AMOUNT = "Excess Amount";

    private final DdnContributionMappingRepository ddnContributionMappingRepository;
    private final VirtualAccountTransactionRepository transactionRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final DdnDataService ddnDataService;

    @Override
    @Transactional
    public AutoTagResponse autoTagContributions(AutoTagRequest request) {
        List<VirtualAccountTransaction> approvedReceipts = getApprovedUntaggedReceipts(request);
        List<AutoTagResultDto> results = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        for (VirtualAccountTransaction receipt : approvedReceipts) {
            try {
                DdnMappingResponse mapping = createAutoMapping(receipt);
                String ddnId = mapping.getInitialCommitmentDdnId() != null
                        ? mapping.getInitialCommitmentDdnId()
                        : mapping.getTopupDdnId();
                results.add(createSuccessResult(mapping.getUtr(), mapping.getFolio(), ddnId, mapping.getTotalTransactionAmount()));
                successCount++;
            } catch (Exception ex) {
                log.warn("Auto-tag skipped for UTR {}: {}", receipt.getUtr(), ex.getMessage());
                results.add(createFailedResult(
                        receipt.getUtr(),
                        resolveFolio(receipt),
                        ex.getMessage()));
                failedCount++;
            }
        }

        return AutoTagResponse.builder()
                .totalProcessed(approvedReceipts.size())
                .successCount(successCount)
                .failedCount(failedCount)
                .results(results)
                .build();
    }

    @Override
    public List<DdnMappingResponse> getAutoTaggedContributions() {
        return ddnContributionMappingRepository.findByMappingTypeAndStatus(MAPPING_TYPE_AUTO, STATUS_ACTIVE)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public List<DdnMappingResponse> getManualMappings() {
        return ddnContributionMappingRepository.findByMappingTypeAndStatus(MAPPING_TYPE_MANUAL, STATUS_ACTIVE)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional
    public DdnMappingResponse createManualMapping(DdnMappingManualRequest request, String userEmail) {
        DdnContributionMapping mapping = createAndSaveMapping(request, userEmail, MAPPING_TYPE_MANUAL);
        return convertToResponse(mapping);
    }

    @Override
    @Transactional
    public BulkDdnMappingResponse bulkUploadMappings(MultipartFile file, String userEmail) {
        validateFile(file);

        List<BulkDdnMappingUploadRow> failedRows = new ArrayList<>();
        List<DdnMappingResponse> successfulMappings = new ArrayList<>();
        int totalRecords = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                totalRecords++;
                BulkDdnMappingUploadRow uploadRow = parseRow(row, i + 1);
                try {
                    DdnContributionMapping mapping = createAndSaveMapping(convertToManualRequest(uploadRow), userEmail, MAPPING_TYPE_BULK);
                    uploadRow.setStatus(STATUS_SUCCESS);
                    successfulMappings.add(convertToResponse(mapping));
                } catch (Exception ex) {
                    uploadRow.setStatus(STATUS_FAILED);
                    uploadRow.setErrorMessage("Row " + (i + 1) + ": " + ex.getMessage());
                    failedRows.add(uploadRow);
                }
            }
        } catch (IOException ex) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "Error reading file: " + ex.getMessage(), ex);
        }

        return BulkDdnMappingResponse.builder()
                .totalRecords(totalRecords)
                .processedRecords(totalRecords)
                .successCount(successfulMappings.size())
                .failedCount(failedRows.size())
                .message(String.format("Processed %d records. Success: %d, Failed: %d",
                        totalRecords, successfulMappings.size(), failedRows.size()))
                .failedRows(failedRows)
                .successfulMappings(successfulMappings)
                .build();
    }

    @Override
    public void downloadBulkMappingTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=DDN_Bulk_Mapping_Template.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("DDN Mapping");
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    HEADER_UTR, HEADER_IFSC, HEADER_TRANSACTION_AMOUNT, HEADER_INITIAL_AMOUNT,
                    HEADER_INITIAL_COMMITMENT_DDN_ID, HEADER_TOPUP_AMOUNT, HEADER_TOPUP_DDN_ID, HEADER_EXCESS_AMOUNT
            };

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    @Override
    public Page<DdnMappingHistoryDto> getMappingHistory(DdnMappingFilterRequest filterRequest) {
        Pageable pageable = createPageable(filterRequest);
        Specification<DdnContributionMapping> spec = DdnMappingSpecification.filterBy(
                filterRequest.getUtr(),
                filterRequest.getMasterVa(),
                filterRequest.getVaAccount(),
                filterRequest.getFolio(),
                filterRequest.getFundId(),
                filterRequest.getDateFrom(),
                filterRequest.getDateTo()
        );
        return ddnContributionMappingRepository.findAll(spec, pageable).map(this::convertToHistoryDto);
    }

    @Override
    public void exportMappingHistory(DdnMappingFilterRequest filterRequest, HttpServletResponse response) throws IOException {
        Specification<DdnContributionMapping> spec = DdnMappingSpecification.filterBy(
                filterRequest.getUtr(),
                filterRequest.getMasterVa(),
                filterRequest.getVaAccount(),
                filterRequest.getFolio(),
                filterRequest.getFundId(),
                filterRequest.getDateFrom(),
                filterRequest.getDateTo()
        );

        List<DdnContributionMapping> mappings = ddnContributionMappingRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "mappedAt"));

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=DDN_Mapping_History_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Mapping History");
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "UTR", "Master VA", "VA Account", "Remarks", "Transaction Source",
                    "Total Transaction Amount", "Folio", "Fund", "Transaction Date Time",
                    "Initial Amount", "Initial DDN ID", "Topup Amount", "Topup DDN ID",
                    "Excess Amount", "Mapping Source", "Mapping Type", "Mapped At"
            };

            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (DdnContributionMapping mapping : mappings) {
                writeHistoryRow(sheet.createRow(rowNum++), mapping);
            }

            workbook.write(response.getOutputStream());
        }
    }

    @Override
    public TransactionAmountResponse getDropdownBasedOnUtrAndIfsc(TransactionAmountRequest request) {
        VirtualAccountTransaction transaction = transactionRepository.findByUtrAndRemitterIfsc(
                        request.getUtr(), request.getIfscCode())
                .orElseThrow(() -> new CoreException(
                        HttpStatus.NOT_FOUND.value(),
                        String.format("Transaction not found for UTR: %s and IFSC: %s", request.getUtr(), request.getIfscCode())));

        if (transaction.getClassification() == Classification.IMPROPER) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(),
                    "Improper transactions cannot be used for DDN mapping. Please classify the transaction as Proper first.");
        }

        String folio = resolveFolio(transaction);
        ensureInvestorCanMap(folio);
        List<DdnOptionDto> openOptions = getOpenDdnOptionsForInvestor(folio).stream()
                .map(option -> enrichDdnOption(option, transaction))
                .toList();

        return TransactionAmountResponse.builder()
                .utr(transaction.getUtr())
                .ifscCode(request.getIfscCode())
                .transactionAmount(parseAmount(transaction.getTransactionAmount()))
                .remitterName(transaction.getRemitterName())
                .remitterAccount(transaction.getRemitterAccount())
                .remitterIfsc(transaction.getRemitterIfsc())
                .bankName(transaction.getBankName())
                .transactionDate(transaction.getTransactionDate())
                .transactionStatus(transaction.getTransactionStatus())
                .investorReference(resolveInvestorReference(transaction))
                .fundName(resolveFundName(transaction))
                .folio(folio)
                .ifscMatched(Boolean.TRUE)
                .initialCommitmentDdns(openOptions.stream()
                        .filter(option -> "Initial".equalsIgnoreCase(option.getCommitmentType()))
                        .toList())
                .topupDdns(openOptions.stream()
                        .filter(option -> "Top-up".equalsIgnoreCase(option.getCommitmentType()))
                        .toList())
                .build();
    }

    private DdnContributionMapping createAndSaveMapping(DdnMappingManualRequest request, String userEmail, String mappingType) {
        validateManualMappingRequest(request);

        Optional<DdnContributionMapping> existingMapping = ddnContributionMappingRepository
                .findByUtrAndIfscCodeAndStatus(request.getUtr(), request.getIfscCode(), STATUS_ACTIVE);
        if (existingMapping.isPresent()) {
            DdnContributionMapping existing = existingMapping.get();
            throw new CoreException(HttpStatus.BAD_REQUEST.value(),
                    String.format("Provided IFSC: %s and UTR: %s already used by %s at %s",
                            request.getIfscCode(),
                            request.getUtr(),
                            existing.getMappedBy() != null ? existing.getMappedBy() : "Unknown",
                            existing.getMappedAt() != null ? existing.getMappedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) : "Unknown"));
        }

        VirtualAccountTransaction transaction = transactionRepository.findByUtrAndRemitterIfsc(request.getUtr(), request.getIfscCode())
                .orElseThrow(() -> new CoreException(HttpStatus.NOT_FOUND.value(),
                        String.format("Transaction not found for UTR: %s and IFSC: %s", request.getUtr(), request.getIfscCode())));

        if (transaction.getClassification() == Classification.IMPROPER) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(),
                    "Improper transactions cannot be used for DDN mapping. Please classify the transaction as Proper first.");
        }

        BigDecimal transactionAmount = parseAmount(transaction.getTransactionAmount());
        if (request.getTransactionAmount() == null || request.getTransactionAmount().compareTo(transactionAmount) != 0) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(),
                    String.format("Transaction amount mismatch. Database amount: %.2f, Entered amount: %.2f",
                            transactionAmount, request.getTransactionAmount()));
        }

        validateUtrUtilization(request.getInitialAmount(), request.getTopupAmount(), request.getExcessAmount(), transactionAmount);

        String folio = resolveFolio(transaction);
        ensureInvestorCanMap(folio);
        validateAndTrackDdnPayment(request.getInitialCommitmentDdnId(), request.getInitialAmount(), folio);
        validateAndTrackDdnPayment(request.getTopupDdnId(), request.getTopupAmount(), folio);

        VirtualAccount va = resolveVirtualAccount(transaction);
        DdnContributionMapping mapping = DdnContributionMapping.builder()
                .utr(request.getUtr())
                .masterVa(va != null ? va.getVaPrefix() : "")
                .vaAccount(transaction.getVirtualAccountNumber())
                .folio(folio)
                .fundId(resolveFundId(transaction))
                .fundName(resolveFundName(transaction))
                .totalTransactionAmount(transactionAmount)
                .transactionDateTime(transaction.getTransactionDate())
                .transactionSource(TRANSACTION_SOURCE_MIS)
                .remarks(request.getRemarks())
                .initialAmount(request.getInitialAmount())
                .ifscCode(request.getIfscCode())
                .initialCommitmentDdnId(request.getInitialCommitmentDdnId())
                .topupAmount(request.getTopupAmount())
                .topupDdnId(request.getTopupDdnId())
                .excessAmount(request.getExcessAmount())
                .mappingSource(userEmail)
                .mappingType(mappingType)
                .status(STATUS_ACTIVE)
                .mappedAt(LocalDateTime.now())
                .mappedBy(userEmail)
                .build();
        return ddnContributionMappingRepository.save(mapping);
    }

    private DdnMappingResponse createAutoMapping(VirtualAccountTransaction receipt) {
        String folio = resolveFolio(receipt);
        ensureInvestorCanMap(folio);
        List<DdnOptionDto> openOptions = getOpenDdnOptionsForInvestor(folio);
        BigDecimal amount = parseAmount(receipt.getTransactionAmount());

        DdnOptionDto matchingOption = openOptions.stream()
                .filter(option -> option.getTotalPayableAmount() != null && option.getTotalPayableAmount().compareTo(amount) == 0)
                .findFirst()
                .orElseThrow(() -> new CoreException(HttpStatus.BAD_REQUEST.value(),
                        "No matching DDN found for the transaction amount."));

        DdnMappingManualRequest request = DdnMappingManualRequest.builder()
                .utr(receipt.getUtr())
                .ifscCode(receipt.getRemitterIfsc())
                .transactionAmount(amount)
                .remarks("Auto tagged using DDN data")
                .initialAmount("Initial".equalsIgnoreCase(matchingOption.getCommitmentType()) ? amount : null)
                .initialCommitmentDdnId("Initial".equalsIgnoreCase(matchingOption.getCommitmentType()) ? matchingOption.getDdnId() : null)
                .topupAmount("Top-up".equalsIgnoreCase(matchingOption.getCommitmentType()) ? amount : null)
                .topupDdnId("Top-up".equalsIgnoreCase(matchingOption.getCommitmentType()) ? matchingOption.getDdnId() : null)
                .build();

        return convertToResponse(createAndSaveMapping(request, MAPPING_SOURCE_SYSTEM, MAPPING_TYPE_AUTO));
    }

    private List<VirtualAccountTransaction> getApprovedUntaggedReceipts(AutoTagRequest request) {
        return transactionRepository.findAll().stream()
                .filter(t -> Classification.PROPER.equals(t.getClassification()))
                .filter(t -> ClassificationStatus.SYSTEM_APPROVED.equals(t.getClassificationStatus()))
                .filter(t -> TRANSACTION_SOURCE_MIS.equalsIgnoreCase(t.getSource()))
                .filter(t -> !ddnContributionMappingRepository.existsByUtrAndStatus(t.getUtr(), STATUS_ACTIVE))
                .filter(t -> request == null || request.getFolio() == null || request.getFolio().equalsIgnoreCase(resolveFolio(t)))
                .filter(t -> request == null || request.getFundId() == null || request.getFundId().equals(resolveFundId(t)))
                .toList();
    }

    private void validateManualMappingRequest(DdnMappingManualRequest request) {
        BigDecimal initial = safeAmount(request.getInitialAmount());
        BigDecimal topup = safeAmount(request.getTopupAmount());
        BigDecimal excess = safeAmount(request.getExcessAmount());

        if (initial.compareTo(BigDecimal.ZERO) == 0 && topup.compareTo(BigDecimal.ZERO) == 0 && excess.compareTo(BigDecimal.ZERO) == 0) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(),
                    "At least one of initial amount, topup amount, or excess amount must be provided");
        }
        if (initial.compareTo(BigDecimal.ZERO) > 0 && isBlank(request.getInitialCommitmentDdnId())) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(),
                    "Initial Commitment DDN ID is required when initial amount is provided");
        }
        if (topup.compareTo(BigDecimal.ZERO) > 0 && isBlank(request.getTopupDdnId())) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(),
                    "Topup DDN ID is required when topup amount is provided");
        }
    }

    private void validateUtrUtilization(BigDecimal initialAmount, BigDecimal topupAmount, BigDecimal excessAmount, BigDecimal transactionAmount) {
        BigDecimal requestedMappingAmount = safeAmount(initialAmount)
                .add(safeAmount(topupAmount))
                .add(safeAmount(excessAmount));
        if (requestedMappingAmount.compareTo(transactionAmount) != 0) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(),
                    String.format("Requested mapping amount must match transaction amount exactly. Transaction amount: %.2f, Requested mapping amount: %.2f",
                            transactionAmount, requestedMappingAmount));
        }
    }

    private void validateAndTrackDdnPayment(String ddnId, BigDecimal amount, String folio) {
        if (isBlank(ddnId) || safeAmount(amount).compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        DdnDataService.DdnDefinition ddn = findDdnDefinition(ddnId)
                .orElseThrow(() -> new CoreException(HttpStatus.NOT_FOUND.value(), "DDN not found: " + ddnId));

        BigDecimal existingPaid = getTotalPaidForDdn(ddnId);
        BigDecimal newTotalPaid = existingPaid.add(amount);
        if (newTotalPaid.compareTo(ddn.totalPayableAmount()) > 0) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(),
                    String.format("Mapped amount exceeds DDN payable. DDN %s - Total payable: %.2f, Already paid: %.2f, New payment: %.2f",
                            ddnId, ddn.totalPayableAmount(), existingPaid, amount));
        }
    }

    private BigDecimal getTotalPaidForDdn(String ddnId) {
        BigDecimal total = ddnContributionMappingRepository.getTotalPaidForDdn(ddnId, STATUS_ACTIVE);
        return total != null ? total : BigDecimal.ZERO;
    }

    private VirtualAccount resolveVirtualAccount(VirtualAccountTransaction transaction) {
        return virtualAccountRepository.findByVaNumberWithInvestor(transaction.getVirtualAccountNumber()).orElse(null);
    }

    private String resolveFolio(VirtualAccountTransaction transaction) {
        VirtualAccount va = resolveVirtualAccount(transaction);
        if (va != null && va.getFolioNumber() != null && !va.getFolioNumber().isBlank()) {
            return va.getFolioNumber();
        }
        String vaNum = transaction.getVirtualAccountNumber();
        throw new CoreException(HttpStatus.BAD_REQUEST.value(),
                "Folio could not be resolved for transaction (VA: " + (vaNum != null ? vaNum : "N/A") + "). Ensure VirtualAccount exists for this VA.");
    }

    private Long resolveFundId(VirtualAccountTransaction transaction) {
        VirtualAccount va = resolveVirtualAccount(transaction);
        if (va != null && va.getFundId() != null) {
            return va.getFundId();
        }
        throw new CoreException(HttpStatus.BAD_REQUEST.value(),
                "Fund could not be resolved for transaction. Ensure VirtualAccount exists for VA: " + transaction.getVirtualAccountNumber());
    }

    private String resolveFundName(VirtualAccountTransaction transaction) {
        String fundName = transaction.getFundName();
        if (fundName != null && !fundName.isBlank()) {
            return fundName;
        }
        VirtualAccount va = resolveVirtualAccount(transaction);
        if (va != null && va.getFundName() != null && !va.getFundName().isBlank()) {
            return va.getFundName();
        }
        return "N/A";
    }

    private String resolveInvestorReference(VirtualAccountTransaction transaction) {
        VirtualAccount va = resolveVirtualAccount(transaction);
        if (va != null && va.getInvestor() != null) {
            return va.getInvestor().getVirtualAccountNumber();
        }
        return null;
    }

    private void ensureInvestorCanMap(String folio) {
        if (!ddnDataService.hasDdnDataForInvestor(folio)) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(),
                    "Investor has no DDN data. Add DDN records for folio: " + folio);
        }
    }

    private List<DdnOptionDto> getOpenDdnOptionsForInvestor(String folio) {
        return ddnDataService.getOpenDdnOptions(folio, this::getTotalPaidForDdn);
    }

    private Optional<DdnDataService.DdnDefinition> findDdnDefinition(String ddnId) {
        return ddnDataService.findDdn(ddnId);
    }

    private String getCurrentDdnStatus(String ddnId) {
        return ddnDataService.currentStatus(ddnId, this::getTotalPaidForDdn);
    }

    private DdnOptionDto enrichDdnOption(DdnOptionDto option, VirtualAccountTransaction transaction) {
        return DdnOptionDto.builder()
                .ddnId(option.getDdnId())
                .calculationId(option.getCalculationId())
                .compositeId(option.getCompositeId())
                .folio(option.getFolio())
                .fundName(option.getFundName())
                .fundId(option.getFundId())
                .totalPayableAmount(option.getTotalPayableAmount())
                .payableFromDate(option.getPayableFromDate())
                .payableToDate(option.getPayableToDate())
                .status(option.getStatus())
                .commitmentType(option.getCommitmentType())
                .transactionAmount(parseAmount(transaction.getTransactionAmount()))
                .utr(transaction.getUtr())
                .ifscCode(transaction.getRemitterIfsc())
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "File is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "File size exceeds maximum limit of 5MB");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "Only Excel (.xlsx) files are supported");
        }
        validateHeaders(file);
    }

    private void validateHeaders(MultipartFile file) {
        String[] expectedHeaders = {
                HEADER_UTR, HEADER_IFSC, HEADER_TRANSACTION_AMOUNT, HEADER_INITIAL_AMOUNT,
                HEADER_INITIAL_COMMITMENT_DDN_ID, HEADER_TOPUP_AMOUNT, HEADER_TOPUP_DDN_ID, HEADER_EXCESS_AMOUNT
        };

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Row headerRow = workbook.getSheetAt(0).getRow(0);
            if (headerRow == null) {
                throw new CoreException(HttpStatus.BAD_REQUEST.value(), "File is empty or header row is missing");
            }
            for (int i = 0; i < expectedHeaders.length; i++) {
                String header = getCellValueAsString(headerRow.getCell(i));
                if (!expectedHeaders[i].equalsIgnoreCase(header)) {
                    throw new CoreException(HttpStatus.BAD_REQUEST.value(),
                            String.format("Invalid header at column %d. Expected: %s", i + 1, expectedHeaders[i]));
                }
            }
        } catch (IOException ex) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "Error reading file to validate headers: " + ex.getMessage(), ex);
        }
    }

    private BulkDdnMappingUploadRow parseRow(Row row, int rowNumber) {
        return BulkDdnMappingUploadRow.builder()
                .rowNumber(rowNumber)
                .utr(getCellValueAsString(row.getCell(0)))
                .ifscCode(getCellValueAsString(row.getCell(1)))
                .transactionAmount(getCellValueAsBigDecimal(row.getCell(2)))
                .initialAmount(getCellValueAsBigDecimal(row.getCell(3)))
                .initialCommitmentDdnId(getCellValueAsString(row.getCell(4)))
                .topupAmount(getCellValueAsBigDecimal(row.getCell(5)))
                .topupDdnId(getCellValueAsString(row.getCell(6)))
                .excessAmount(getCellValueAsBigDecimal(row.getCell(7)))
                .build();
    }

    private DdnMappingManualRequest convertToManualRequest(BulkDdnMappingUploadRow uploadRow) {
        return DdnMappingManualRequest.builder()
                .utr(uploadRow.getUtr())
                .ifscCode(uploadRow.getIfscCode())
                .transactionAmount(uploadRow.getTransactionAmount())
                .initialAmount(uploadRow.getInitialAmount())
                .initialCommitmentDdnId(uploadRow.getInitialCommitmentDdnId())
                .topupAmount(uploadRow.getTopupAmount())
                .topupDdnId(uploadRow.getTopupDdnId())
                .excessAmount(uploadRow.getExcessAmount())
                .build();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> null;
        };
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            }
            if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue();
                return value == null || value.isBlank() ? null : new BigDecimal(value.trim());
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private Pageable createPageable(DdnMappingFilterRequest filterRequest) {
        int page = filterRequest.getPage() != null ? filterRequest.getPage() : 0;
        int size = filterRequest.getSize() != null ? filterRequest.getSize() : 20;
        String sortBy = filterRequest.getSortBy() != null ? filterRequest.getSortBy() : "mappedAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(filterRequest.getSortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    private DdnMappingResponse convertToResponse(DdnContributionMapping mapping) {
        return DdnMappingResponse.builder()
                .id(mapping.getId())
                .utr(mapping.getUtr())
                .masterVa(mapping.getMasterVa())
                .vaAccount(mapping.getVaAccount())
                .folio(mapping.getFolio())
                .fundId(mapping.getFundId())
                .fundName(mapping.getFundName())
                .totalTransactionAmount(mapping.getTotalTransactionAmount())
                .transactionDateTime(mapping.getTransactionDateTime())
                .transactionSource(mapping.getTransactionSource())
                .remarks(mapping.getRemarks())
                .initialAmount(mapping.getInitialAmount())
                .ifscCode(mapping.getIfscCode())
                .initialCommitmentDdnId(mapping.getInitialCommitmentDdnId())
                .topupAmount(mapping.getTopupAmount())
                .topupDdnId(mapping.getTopupDdnId())
                .excessAmount(mapping.getExcessAmount())
                .mappingSource(mapping.getMappingSource())
                .mappingType(mapping.getMappingType())
                .status(resolveMappingStatus(mapping))
                .mappedAt(mapping.getMappedAt())
                .mappedBy(mapping.getMappedBy())
                .build();
    }

    private DdnMappingHistoryDto convertToHistoryDto(DdnContributionMapping mapping) {
        return DdnMappingHistoryDto.builder()
                .id(mapping.getId())
                .utr(mapping.getUtr())
                .masterVa(mapping.getMasterVa())
                .vaAccount(mapping.getVaAccount())
                .remarks(mapping.getRemarks())
                .transactionSource(mapping.getTransactionSource())
                .totalTransactionAmount(mapping.getTotalTransactionAmount())
                .folio(mapping.getFolio())
                .fundName(mapping.getFundName())
                .transactionDateTime(mapping.getTransactionDateTime())
                .initialAmount(mapping.getInitialAmount())
                .initialCommitmentDdnId(mapping.getInitialCommitmentDdnId())
                .topupAmount(mapping.getTopupAmount())
                .topupDdnId(mapping.getTopupDdnId())
                .excessAmount(mapping.getExcessAmount())
                .mappingSource(mapping.getMappingSource())
                .mappingType(mapping.getMappingType())
                .mappedAt(mapping.getMappedAt())
                .mappedBy(mapping.getMappedBy())
                .build();
    }

    private String resolveMappingStatus(DdnContributionMapping mapping) {
        String ddnId = mapping.getInitialCommitmentDdnId() != null
                ? mapping.getInitialCommitmentDdnId()
                : mapping.getTopupDdnId();
        if (ddnId == null) {
            return mapping.getStatus();
        }
        return getCurrentDdnStatus(ddnId);
    }

    private void writeHistoryRow(Row row, DdnContributionMapping mapping) {
        int col = 0;
        row.createCell(col++).setCellValue(nullToEmpty(mapping.getUtr()));
        row.createCell(col++).setCellValue(nullToEmpty(mapping.getMasterVa()));
        row.createCell(col++).setCellValue(nullToEmpty(mapping.getVaAccount()));
        row.createCell(col++).setCellValue(nullToEmpty(mapping.getRemarks()));
        row.createCell(col++).setCellValue(nullToEmpty(mapping.getTransactionSource()));
        row.createCell(col++).setCellValue(mapping.getTotalTransactionAmount() != null ? mapping.getTotalTransactionAmount().doubleValue() : 0.0);
        row.createCell(col++).setCellValue(nullToEmpty(mapping.getFolio()));
        row.createCell(col++).setCellValue(nullToEmpty(mapping.getFundName()));
        row.createCell(col++).setCellValue(mapping.getTransactionDateTime() != null ? mapping.getTransactionDateTime().toString() : "");
        row.createCell(col++).setCellValue(mapping.getInitialAmount() != null ? mapping.getInitialAmount().doubleValue() : 0.0);
        row.createCell(col++).setCellValue(nullToEmpty(mapping.getInitialCommitmentDdnId()));
        row.createCell(col++).setCellValue(mapping.getTopupAmount() != null ? mapping.getTopupAmount().doubleValue() : 0.0);
        row.createCell(col++).setCellValue(nullToEmpty(mapping.getTopupDdnId()));
        row.createCell(col++).setCellValue(mapping.getExcessAmount() != null ? mapping.getExcessAmount().doubleValue() : 0.0);
        row.createCell(col++).setCellValue(nullToEmpty(mapping.getMappingSource()));
        row.createCell(col++).setCellValue(nullToEmpty(mapping.getMappingType()));
        row.createCell(col).setCellValue(mapping.getMappedAt() != null ? mapping.getMappedAt().toString() : "");
    }

    private AutoTagResultDto createSuccessResult(String utr, String folio, String ddnId, BigDecimal amount) {
        return AutoTagResultDto.builder()
                .utr(utr)
                .folio(folio)
                .ddnId(ddnId)
                .amount(amount)
                .status(STATUS_SUCCESS)
                .message(STATUS_CLOSED.equals(getCurrentDdnStatus(ddnId))
                        ? "Successfully tagged and DDN closed"
                        : "Successfully tagged")
                .build();
    }

    private AutoTagResultDto createFailedResult(String utr, String folio, String message) {
        return AutoTagResultDto.builder()
                .utr(utr)
                .folio(folio)
                .status(STATUS_FAILED)
                .message(message)
                .build();
    }

    private BigDecimal parseAmount(String amount) {
        try {
            return new BigDecimal(amount);
        } catch (Exception ex) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "Invalid transaction amount: " + amount);
        }
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

