package com.jahnavi.contribution.contribution_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DdnMappingManualRequest {
    
    @NotBlank(message = "UTR is required")
    private String utr;

    @NotNull(message = "Transaction amount is required")
    private BigDecimal transactionAmount;

    @NotBlank(message = "Ifsc Code is required")
    private String ifscCode;

    private BigDecimal initialAmount;

    private String initialCommitmentDdnId;

    private BigDecimal topupAmount;

    private String topupDdnId;

    private BigDecimal excessAmount;

    private String remarks;
}

