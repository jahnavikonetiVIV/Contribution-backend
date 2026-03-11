package com.jahnavi.contribution.contribution_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for MIS Record from email or webhook
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MisRecordDto {

    private Long id;
    private String utr;
    private String investorReference;
    private String bankAccountNumber;
    private String ifsc;
    private BigDecimal amount;
    private LocalDateTime transactionDateTime;
    private String fundName;
    private String paymentMode;
    private String source; // EMAIL or WEBHOOK
    private String senderEmailId;
    private LocalDateTime receivedAt;
    private Boolean isDuplicate;
    private String remarks;
    private String remitterName;
    private String remitterAccount;
}




