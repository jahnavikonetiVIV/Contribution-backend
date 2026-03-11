package com.jahnavi.contribution.contribution_management.service.impl;

import com.jahnavi.contribution.contribution_management.dto.IngestionResponseDto;
import com.jahnavi.contribution.contribution_management.dto.MisIngestionDto;
import com.jahnavi.contribution.contribution_management.entity.MisUpload;
import com.jahnavi.contribution.contribution_management.entity.RawCreditCombined;
import com.jahnavi.contribution.contribution_management.repository.MisUploadRepository;
import com.jahnavi.contribution.contribution_management.repository.RawCreditRepository;
import com.jahnavi.contribution.contribution_management.service.RawCreditService;
import com.vivriti.investron.common.exception.CoreException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RawCreditServiceImpl implements RawCreditService {

    private static final String SOURCE_COMBINED = "COMBINED";
    private static final String SOURCE_WEBHOOK = "WEBHOOK";
    private static final String CLASSIFICATION_WEBHOOK = "Webhook";

    private final RawCreditRepository rawCreditRepository;
    private final MisUploadRepository misUploadRepository;

    public static Specification<RawCreditCombined> searchByTextAndSource(
            String search,
            String source) {

        return (root, query, cb) -> {

            Predicate predicate = cb.conjunction();

            if (source != null && !source.isBlank()) {
                if (SOURCE_COMBINED.equalsIgnoreCase(source)) {
                    predicate = cb.and(
                            predicate,
                            root.get("source").in(SOURCE_WEBHOOK, "EMAIL")
                    );
                } else {
                    predicate = cb.and(
                            predicate,
                            cb.equal(root.get("source"), source)
                    );
                    predicate = cb.and(
                            predicate,
                            cb.equal(root.get("isDuplicate"), false)
                    );
                }
            }


            if (search != null && !search.isBlank()) {
                String likeSearch = "%" + search.toLowerCase() + "%";

                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("utr")), likeSearch),
                        cb.like(cb.lower(root.get("virtualAccountNumber")), likeSearch)
                );

                predicate = cb.and(predicate, searchPredicate);
            }

            return predicate;
        };
    }
    public List<IngestionResponseDto> fetchWebhookOrCombinedRecords(String classification, String search) {

        String classificationNormalized = classification != null ? classification.trim() : "";
        String source = CLASSIFICATION_WEBHOOK.equalsIgnoreCase(classificationNormalized)
                ? SOURCE_WEBHOOK
                : SOURCE_COMBINED;

        List<RawCreditCombined> fetchedData =
                rawCreditRepository.findAll(
                        searchByTextAndSource(search, source),
                        Sort.by(Sort.Direction.DESC, "receivedAt")  // Latest first
                );

        return fetchedData.stream()
                .map(entity -> IngestionResponseDto.builder()
                        .utr(entity.getUtr())
                        .virtualAccountNumber(entity.getVirtualAccountNumber())
                        .fundName(entity.getFundName())
                        .bankAccount(entity.getRemitterAccount())
                        .ifscCode(entity.getRemitterIfsc())
                        .amount(entity.getTransactionAmount())
                        .paymentMode(entity.getTransactionType())
                        .duplicate(entity.getIsDuplicate())
                        .source(entity.getSource())
                        .recordId(entity.getId())
                        .recievedAt(entity.getReceivedAt().toString())
                        .build()
                )
                .toList();
    }

    public byte[] fetchRecordsForDownload(String classification) throws IOException {
        validateClassification(classification);
        boolean isWebhook = CLASSIFICATION_WEBHOOK.equalsIgnoreCase(classification != null ? classification.trim() : "");
        String source = isWebhook ? SOURCE_WEBHOOK : SOURCE_COMBINED;
        String sheetName = isWebhook ? "Webhook Records" : "Combined Records";
        String[] headers = isWebhook
                ? new String[]{"UTR", "Bank Account Number", "IFSC", "Amount", "Date Time", "Fund Name", "Payment Mode"}
                : new String[]{"UTR", "Bank Account Number", "IFSC", "Amount", "Date Time", "Fund Name", "Payment Mode", "Record Number", "Duplicate", "VA"};

        SXSSFWorkbook wb = new SXSSFWorkbook(100);
        Sheet sheet = wb.createSheet(sheetName);
        writeHeaderRow(wb, sheet, headers);

        int pageNumber = 0;
        int rowNum = 1;
        int pageSize = 20;
        Page<RawCreditCombined> pageResult;

        do {
            pageResult = rawCreditRepository.findAll(
                    searchByTextAndSource("", source),
                    PageRequest.of(pageNumber, pageSize, Sort.by("id").ascending())
            );
            for (RawCreditCombined creditRecord : pageResult.getContent()) {
                Row row = sheet.createRow(rowNum++);
                writeRecordToRow(row, creditRecord, isWebhook);
            }
            pageNumber++;
        } while (pageResult.hasNext());
        if (rowNum <= 1) {
            wb.dispose();
            String recordType = isWebhook ? CLASSIFICATION_WEBHOOK : "Combined";
            throw new CoreException(HttpStatus.NOT_FOUND.value(),
                    String.format("No %s records found for download. Please ensure there are %s records before downloading.", recordType, recordType));
        }

        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        wb.write(bas);
        wb.dispose();
        return bas.toByteArray();
    }

    private static void validateClassification(String classification) {
        String normalized = classification != null ? classification.trim() : "";
        if (!CLASSIFICATION_WEBHOOK.equalsIgnoreCase(normalized) && !"Combined".equalsIgnoreCase(normalized)) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "Invalid classification please use Either Webhook or Combined");
        }
    }

    private static void writeHeaderRow(Workbook wb, Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private static void writeRecordToRow(Row row, RawCreditCombined creditRecord, boolean isWebhook) {
        row.createCell(0).setCellValue(creditRecord.getUtr());
        row.createCell(1).setCellValue(creditRecord.getRemitterAccount());
        row.createCell(2).setCellValue(creditRecord.getRemitterIfsc());
        row.createCell(3).setCellValue(creditRecord.getTransactionAmount());
        row.createCell(4).setCellValue(creditRecord.getTransactionTime());
        row.createCell(5).setCellValue(creditRecord.getFundName());
        row.createCell(6).setCellValue(creditRecord.getTransactionType());
        if (!isWebhook) {
            row.createCell(7).setCellValue(creditRecord.getId());
            row.createCell(8).setCellValue(creditRecord.getIsDuplicate());
            row.createCell(9).setCellValue(creditRecord.getVirtualAccountNumber());
        }
    }

    public List<MisIngestionDto> fetchMisRecords(){
        List<MisUpload> misRecords = misUploadRepository.findAll(Sort.by("id").descending());

        return misRecords.stream().map(each -> MisIngestionDto.builder()
                .senderEmail(each.getSenderEmail())
                .fundName(each.getFundName())
                .attachment(each.getAttachmentName())
                .status(each.getStatus())
                .errorReason(each.getErrorReason())
                .dateRecieved(each.getDateReceived())
                .fileId(each.getMisFileId())
                .build()
        ).toList();
    }
}

