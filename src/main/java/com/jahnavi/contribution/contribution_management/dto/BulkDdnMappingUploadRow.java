package com.jahnavi.contribution.contribution_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkDdnMappingUploadRow {
    private Integer rowNumber;
    private String utr;
    private String ifscCode;
    private BigDecimal transactionAmount;
    private BigDecimal initialAmount;
    private String initialCommitmentDdnId;
    private BigDecimal topupAmount;
    private String topupDdnId;
    private BigDecimal excessAmount;
    private String status;
    private String errorMessage;
}

