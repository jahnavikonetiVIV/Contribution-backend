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
public class BulkFundRequestMappingUploadRow {
    private Integer rowNumber;
    private String utr;
    private String ifscCode;
    private BigDecimal transactionAmount;
    private BigDecimal initialAmount;
    private String initialCommitmentFundRequestId;
    private BigDecimal topupAmount;
    private String topupFundRequestId;
    private BigDecimal excessAmount;
    private String status;
    private String errorMessage;
}
