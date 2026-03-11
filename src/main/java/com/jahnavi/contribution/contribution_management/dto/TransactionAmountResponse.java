package com.jahnavi.contribution.contribution_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for transaction amount lookup
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionAmountResponse {
    
    private String utr;
    private String ifscCode;
    private BigDecimal transactionAmount;
    private String remitterName;
    private String remitterAccount;
    private String remitterIfsc;
    private String bankName;
    private LocalDateTime transactionDate;
    private String transactionStatus;
    private String investorReference;
    private String fundName;
    private String folio; // Extracted folio number
    private Boolean ifscMatched; // Indicates if IFSC validation passed
    
    // DDN Dropdown Options based on folio
    private List<DdnOptionDto> initialCommitmentDdns;
    private List<DdnOptionDto> topupDdns;
}
