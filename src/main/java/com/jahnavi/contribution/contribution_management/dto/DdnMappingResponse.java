package com.jahnavi.contribution.contribution_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DdnMappingResponse {
    private Long id;
    private String utr;
    private String masterVa;
    private String vaAccount;
    private String folio;
    private Long fundId;
    private String fundName;
    private BigDecimal totalTransactionAmount;
    private LocalDateTime transactionDateTime;
    private String transactionSource;
    private String remarks;
    
    private BigDecimal initialAmount;
    private String ifscCode;
    private String initialCommitmentDdnId;
    private BigDecimal topupAmount;
    private String topupDdnId;
    private BigDecimal excessAmount;
    
    private String mappingSource;
    private String mappingType;
    private String status;
    private LocalDateTime mappedAt;
    private String mappedBy;
}

