package com.jahnavi.contribution.contribution_management.service.impl;


import com.jahnavi.contribution.contribution_management.entity.InvestorData;
import com.jahnavi.contribution.contribution_management.entity.RawCreditCombined;
import com.jahnavi.contribution.contribution_management.entity.VirtualAccount;
import com.jahnavi.contribution.contribution_management.enums.Classification;
import com.jahnavi.contribution.contribution_management.enums.ClassificationStatus;
import com.jahnavi.contribution.contribution_management.repository.InvestorDataRepository;
import com.jahnavi.contribution.contribution_management.repository.RawCreditCombinedRepository;
import com.jahnavi.contribution.contribution_management.util.EmailTemplateLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContributionCommonMethodsImpl {


    private static final String FUND_RECEIPT_CONFIRMATION_TEMPLATE = "contribution/fund-receipt-confirmation-template.html";


    private final RawCreditCombinedRepository rawRepo;
    private final InvestorDataRepository investorDataRepository;


    public Long processAttachment(MultipartFile attachment) {
        String fileName = attachment.getOriginalFilename();
        log.info("Processing attachment: {}", fileName);
        if (fileName != null && (fileName.toLowerCase().endsWith(".xlsx") ||
                fileName.toLowerCase().endsWith(".xls") ||
                fileName.toLowerCase().endsWith(".csv"))) {
            log.info("DMS upload not configured - storing null fileId for attachment: {}", fileName);
            return null;
        }
        log.warn("Skipping non-Excel/CSV attachment: {}", fileName);
        return null;
    }

    public Long getLongValue(Object value) {
        if (value instanceof Integer integer) {
            return integer.longValue();
        } else if (value instanceof Long l) {
            return l;
        }
        return null;
    }

    public boolean isDuplicate(String utr, String ifsc) {
        if (utr == null || ifsc == null || ifsc.length() < 4) {
            return false;
        }
        String ifscPrefix = ifsc.substring(0, 4);
        return rawRepo.existsByUtrAndIfscPrefix(utr, ifscPrefix);
    }

    public void classifyTransaction(RawCreditCombined entity, VirtualAccount va) {
        if (va == null) {
            entity.setClassification(Classification.IMPROPER);
            entity.setClassificationStatus(ClassificationStatus.HOLD);
            entity.setReason("Virtual Account not found");
            return;
        }

        String vaNumber = va.getVaNumber();
        if (vaNumber == null || vaNumber.isBlank()) {
            entity.setClassification(Classification.IMPROPER);
            entity.setClassificationStatus(ClassificationStatus.HOLD);
            entity.setReason("Virtual Account number not found");
            return;
        }

        Optional<InvestorData> investorDataOpt =
                investorDataRepository.findByVirtualAccountNumberAndActiveTrue(vaNumber);

        if (investorDataOpt.isEmpty()) {
            entity.setClassification(Classification.IMPROPER);
            entity.setClassificationStatus(ClassificationStatus.HOLD);
            entity.setReason("Investor data not found for virtual account");
            return;
        }

        InvestorData investorData = investorDataOpt.get();

        String remitterAccount = entity.getRemitterAccount();
        if (remitterAccount == null || remitterAccount.isBlank()) {
            entity.setClassification(Classification.IMPROPER);
            entity.setClassificationStatus(ClassificationStatus.HOLD);
            entity.setReason("Remitter account number missing");
            return;
        }

        boolean matchFound = remitterAccount.equalsIgnoreCase(investorData.getBankAccountNumber());

        if (matchFound) {
            entity.setClassification(Classification.PROPER);
            entity.setClassificationStatus(ClassificationStatus.SYSTEM_APPROVED);
            entity.setReason("Registered Source Account");
            sendFundReceiptConfirmationMail(entity, va, investorData);
        } else {
            entity.setClassification(Classification.IMPROPER);
            entity.setClassificationStatus(ClassificationStatus.HOLD);
            entity.setReason("Non-Registered Source Account");
        }
    }

    /**
     * Sends Fund Receipt Confirmation email to the investor when contribution is classified as PROPER.
     * Public so bulk upload can send mails only after all records are successfully processed.
     */
    public void sendFundReceiptConfirmationMailForContribution(RawCreditCombined entity, VirtualAccount va,
                                                              InvestorData investorData) {
        sendFundReceiptConfirmationMail(entity, va, investorData);
    }

    /**
     * Demo mode: Fund receipt confirmation mail is disabled.
     * Sends Fund Receipt Confirmation email to the investor when contribution is classified as PROPER.
     */
    private void sendFundReceiptConfirmationMail(RawCreditCombined entity, VirtualAccount va,
                                                 InvestorData investorData) {
        log.debug("Demo mode: Skipping fund receipt confirmation mail for UTR {}", entity.getUtr());
    }

    private static String deriveFirstName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Investor";
        }
        String trimmed = fullName.trim();
        int space = trimmed.indexOf(' ');
        return space > 0 ? trimmed.substring(0, space) : trimmed;
    }

    /**
     * Loads fund receipt confirmation template from resources and substitutes placeholders.
     */
    private static String loadAndFillFundReceiptTemplate(String investorFirstName, String amount, String dateStr, String fundName) throws IOException {
        String template = EmailTemplateLoader.loadTemplate(FUND_RECEIPT_CONFIRMATION_TEMPLATE);
        return template
                .replace("{{investorFirstName}}", escapeHtml(investorFirstName))
                .replace("{{amount}}", escapeHtml(amount))
                .replace("{{date}}", escapeHtml(dateStr))
                .replace("{{fundName}}", escapeHtml(fundName));
    }

    private static String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

}
