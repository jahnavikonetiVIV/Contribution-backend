package com.jahnavi.contribution.contribution_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for fetching transaction amount by UTR and IFSC
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionAmountRequest {
    
    @NotBlank(message = "UTR is required")
    private String utr;
    
    @NotBlank(message = "IFSC code is required")
    private String ifscCode;
}
