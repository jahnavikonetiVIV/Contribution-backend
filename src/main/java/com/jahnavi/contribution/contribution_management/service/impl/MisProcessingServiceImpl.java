package com.jahnavi.contribution.contribution_management.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jahnavi.contribution.contribution_management.dto.EcollectTransactionDto;
import com.jahnavi.contribution.contribution_management.entity.MisUpload;
import com.jahnavi.contribution.contribution_management.entity.RawCreditCombined;
import com.jahnavi.contribution.contribution_management.entity.VirtualAccount;
import com.jahnavi.contribution.contribution_management.entity.VirtualAccountTransaction;
import com.jahnavi.contribution.contribution_management.helper.MisReportParserHelper;
import com.jahnavi.contribution.contribution_management.repository.MisUploadRepository;
import com.jahnavi.contribution.contribution_management.repository.RawCreditCombinedRepository;
import com.jahnavi.contribution.contribution_management.repository.VirtualAccountRepository;
import com.jahnavi.contribution.contribution_management.repository.VirtualAccountTransactionRepository;
import com.jahnavi.contribution.exception.CoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class MisProcessingServiceImpl {

    private static final String PARSE_ERROR = "PARSE_ERROR";

    private final VirtualAccountRepository virtualAccountRepository;
    private final MisReportParserHelper misReportParserHelper;
    private final ContributionCommonMethodsImpl contributionCommonMethods;
    private final MisUploadRepository misUploadRepository;
    private final ObjectMapper objectMapper;
    private final VirtualAccountTransactionRepository virtualAccountTransactionRepository;
    private final RawCreditCombinedRepository rawRepo;


    @Transactional
    public void processMisReportFile(MultipartFile file,
                                     Long messageId,
                                     String s3Path,
                                     String senderEmail) throws IOException {

        MisUpload misUpload = createInitialMisUpload(file, senderEmail);
        log.info("data {}",misUpload);
        Long misBatchId = misUpload.getId();
        Set<String> fundNames = new LinkedHashSet<>();

        try {
            List<EcollectTransactionDto> transactions = parseTransactions(file);

            if (!processTransactions(transactions, misBatchId, fundNames)) {
                return;
            }

            markBatchCompleted(misUpload, fundNames);

        } catch (Exception ex) {
            handleProcessingException(ex, misUpload, misBatchId, fundNames);
        }
    }
    private MisUpload createInitialMisUpload(MultipartFile file, String senderEmail) {

        Long fileId = contributionCommonMethods.processAttachment(file);

        MisUpload misUpload = new MisUpload();
        misUpload.setAttachmentName(file.getOriginalFilename());
        misUpload.setDateReceived(LocalDate.now());
        misUpload.setStatus("PROCESSING");
        misUpload.setMisFileId(fileId);
        misUpload.setSenderEmail(
                senderEmail != null && !senderEmail.isBlank() ? senderEmail : "N/A"
        );
        misUpload.setFundName("-");

        return misUploadRepository.save(misUpload);
    }
    private List<EcollectTransactionDto> parseTransactions(MultipartFile file) throws IOException {
        return misReportParserHelper.parseFile(
                file.getInputStream(),
                file.getOriginalFilename()
        );
    }
    private boolean processTransactions(List<EcollectTransactionDto> transactions,
                                        Long misBatchId,
                                        Set<String> fundNames) {

        for (EcollectTransactionDto dto : transactions) {
            if (!processTransaction(dto, misBatchId, fundNames)) {
                return false;
            }
        }
        return true;
    }
    private void markBatchCompleted(MisUpload misUpload, Set<String> fundNames) {

        misUpload.setFundName(resolveFundNames(fundNames));
        misUpload.setStatus("COMPLETED");
        misUploadRepository.save(misUpload);
    }
    private void handleProcessingException(Exception ex,
                                           MisUpload misUpload,
                                           Long misBatchId,
                                           Set<String> fundNames) {

        log.error("MIS file processing failed for batch {} | attachment={}",
                misBatchId,
                misUpload.getAttachmentName(),
                ex);

        misUpload.setFundName(resolveFundNames(fundNames));

        if (isParseOrCorruptionError(ex)) {
            handleParseError(ex, misUpload, misBatchId);
            return;
        }

        handleGenericFailure(misUpload);
    }
    private void handleGenericFailure(MisUpload misUpload) {

        misUpload.setStatus("FAILED");
        misUpload.setErrorReason("File is corrupted or format is invalid");

        misUploadRepository.save(misUpload);
        sendMisFailureAlert(misUpload, "FAILED");
    }
    private String resolveFundNames(Set<String> fundNames) {
        return fundNames.isEmpty() ? "N/A" : String.join(", ", fundNames);
    }

    private String truncate(String value, int maxLength) {
        return value.length() > maxLength
                ? value.substring(0, maxLength)
                : value;
    }
    private void handleParseError(Exception ex,
                                  MisUpload misUpload,
                                  Long misBatchId) {

        rollbackBatchData(misBatchId);

        misUpload.setStatus(PARSE_ERROR);

        String reason = ex.getMessage() != null
                ? ex.getMessage()
                : "File is corrupted or mandatory fields missing";

        misUpload.setErrorReason(truncate(reason, 500));

        misUploadRepository.save(misUpload);
        sendMisFailureAlert(misUpload, PARSE_ERROR);
    }


    private boolean processTransaction(EcollectTransactionDto dto, Long misBatchId, Set<String> fundNames) {
        try {
            validateMandatoryFields(dto);
            VirtualAccount va = virtualAccountRepository.findByVaNumber(dto.getVirtualAccountNo()).orElse(null);
            addFundNameIfPresent(va, fundNames);

            RawCreditCombined entity = buildRawCreditEntity(dto, va, misBatchId);
            boolean duplicate = contributionCommonMethods.isDuplicate(dto.getTransferUniqueNo(), dto.getRmtrAccountIfsc());
            entity.setIsDuplicate(duplicate);

            if (duplicate) {
                applyQuarantinedStatus(entity);
            } else {
                applyReceivedStatus(entity, va, misBatchId);
            }
            rawRepo.save(entity);
            return true;
        } catch (Exception rowEx) {
            log.error("Row failed for UTR {} : {}", dto.getTransferUniqueNo() != null ? dto.getTransferUniqueNo() : "N/A", rowEx.getMessage(), rowEx);
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "Mandatory fields missing or invalid: " + rowEx.getMessage(), rowEx);
        }
    }

    private void addFundNameIfPresent(VirtualAccount va, Set<String> fundNames) {
        if (va == null || va.getFundName() == null || va.getFundName().isBlank()) {
            return;
        }
        fundNames.add(va.getFundName());
    }

    private RawCreditCombined buildRawCreditEntity(EcollectTransactionDto dto, VirtualAccount va, Long misBatchId) throws JsonProcessingException {
        return RawCreditCombined.builder()
                .utr(dto.getTransferUniqueNo())
                .transactionId(dto.getTransferUniqueNo())
                .virtualAccount(va)
                .virtualAccountNumber(dto.getVirtualAccountNo())
                .fundId(va != null ? va.getFundId() : null)
                .rawPayload(objectMapper.writeValueAsString(dto))
                .transactionAmount(dto.getTransferAmt())
                .transactionTime(dto.getTransferTimestamp())
                .remitterName(dto.getRmtrFullName())
                .remitterAccount(dto.getRmtrAccountNo())
                .remitterIfsc(dto.getRmtrAccountIfsc())
                .transactionType(dto.getTransferType())
                .receivedAt(LocalDateTime.now())
                .fundName(va != null ? va.getFundName() : null)
                .source("EMAIL")
                .misBatchId(misBatchId)
                .build();
    }

    private void applyQuarantinedStatus(RawCreditCombined entity) {
        entity.setProcessingStatus("QUARANTINED");
        entity.setErrorMessage("Duplicate UTR at fund level");
    }

    private void applyReceivedStatus(RawCreditCombined entity, VirtualAccount va, Long misBatchId) {
        contributionCommonMethods.classifyTransaction(entity, va);
        entity.setProcessingStatus("RECEIVED");

            VirtualAccountTransaction vat = getVirtualAccountTransaction(entity, va, misBatchId);
            virtualAccountTransactionRepository.save(vat);

    }

    private boolean isParseOrCorruptionError(Throwable ex) {
        if (ex == null) {
            return false;
        }
        if (ex instanceof CoreException ) {
            return true;
        }
        return parseErrorMessageIndicatesError(ex.getMessage());
    }

    private static boolean parseErrorMessageIndicatesError(String msg) {
        if (msg == null) {
            return false;
        }
        String lower = msg.toLowerCase();
        return lower.contains("parsing failed") || lower.contains("mandatory") || lower.contains("corrupted")
                || lower.contains("unsupported file") || lower.contains("file name missing")
                || lower.contains("unparseable date") || lower.contains("excel parsing")
                || lower.contains("csv parsing");
    }

    private void rollbackBatchData(Long misBatchId) {
        try {
            virtualAccountTransactionRepository.deleteByMisBatchId(misBatchId);
            rawRepo.deleteByMisBatchId(misBatchId);
            log.info("Rolled back batch data for misBatchId={}", misBatchId);
        } catch (Exception e) {
            log.error("Error rolling back batch data for misBatchId={}", misBatchId, e);
        }
    }

    /**
     * Sends MIS failure alert mail to all users with ROLE_OPS_HEAD and ROLE_SUPER_ADMIN.
     * Used when batch is saved as PARSE_ERROR or FAILED in MIS upload table.
     * Template: Sub: Issue found in MIS ingestion for contribution mapping from bank
     *           Body: Issue found in MIS ingestion for contribution mapping from bank which received from &lt;email_id&gt; at &lt;date time&gt;
     */
    /**
     * Demo mode: MIS failure/parse error mails are disabled. Only logs locally.
     */
    private void sendMisFailureAlert(MisUpload misUpload, String status) {
        log.info("Demo mode: Skipping MIS failure alert mail for batch {} (status={}). Parse/failure is logged only.", misUpload.getId(), status);
    }

    /**
     * Collects unique email addresses of users with ROLE_OPS_HEAD or ROLE_SUPER_ADMIN.
     */


    @NotNull
    private static VirtualAccountTransaction getVirtualAccountTransaction(
            RawCreditCombined entity,
            VirtualAccount va,
            Long misBatchId) {

        VirtualAccountTransaction vat = new VirtualAccountTransaction();
        vat.setUtr(entity.getUtr());
        vat.setClientAccount(entity.getRemitterAccount());
        vat.setRemitterIfsc(entity.getRemitterIfsc());
        vat.setRemitterAccount(entity.getRemitterAccount());
        vat.setRemitterName(entity.getRemitterName());
        vat.setTransactionDate(entity.getTransactionTime());

        vat.setVirtualAccountNumber(
                va != null ? va.getVaNumber() : entity.getVirtualAccountNumber()
        );

        vat.setClassification(entity.getClassification());
        vat.setClassificationStatus(entity.getClassificationStatus());
        vat.setTransactionAmount(entity.getTransactionAmount());
        vat.setTransactionType(entity.getTransactionType());
        vat.setMisBatchId(misBatchId);
        vat.setReason(entity.getReason());
        vat.setSource("EMAIL");
        vat.setProcessingStatus(entity.getProcessingStatus());

        return vat;
    }

    private void validateMandatoryFields(EcollectTransactionDto dto) {
        if (dto.getTransferUniqueNo() == null || dto.getTransferUniqueNo().isBlank()) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "Utr number is mandatory");
        }

        if (dto.getRmtrAccountNo() == null || dto.getRmtrAccountNo().isBlank()) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "Remitter account number is mandatory");
        }

        if (dto.getRmtrAccountIfsc() == null || dto.getRmtrAccountIfsc().isBlank()) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "Remitter IFSC is mandatory");
        }

        if (dto.getTransferAmt() == null) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "Transaction amount is mandatory");
        }
    }
}
