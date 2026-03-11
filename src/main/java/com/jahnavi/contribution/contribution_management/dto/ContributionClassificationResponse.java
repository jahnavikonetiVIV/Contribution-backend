package com.jahnavi.contribution.contribution_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContributionClassificationResponse {
    private String utr;
    private String vaNumber;
    private String folioNumber;
    private BigDecimal amount;
    private String classification;
    private String reason;
    private String state;
    private String bankAccountNumber;
    private String ifsc;
    private String paymentMode;
    private LocalDateTime transactionDateTime;
    private String remarks;
    private String remitterName;
}

