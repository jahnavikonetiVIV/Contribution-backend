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
public class DdnMappingHistoryDto {
    private Long id;
    private String utr;
    private String masterVa;
    private String vaAccount;
    private String remarks;
    private String transactionSource;
    private BigDecimal totalTransactionAmount;
    private String folio;
    private String fundName;
    private LocalDateTime transactionDateTime;
    private BigDecimal initialAmount;
    private String initialCommitmentDdnId;
    private BigDecimal topupAmount;
    private String topupDdnId;
    private BigDecimal excessAmount;
    private String mappingSource;
    private String mappingType;
    private LocalDateTime mappedAt;
    private String mappedBy;
}

