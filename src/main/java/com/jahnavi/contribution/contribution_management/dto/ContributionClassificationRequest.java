package com.jahnavi.contribution.contribution_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ContributionClassificationRequest {

    @NotBlank(message = "UTR is required")
    private String utr;

    private String vaNumber; // optional; may be derived from remarks for cheque

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private String bankAccountNumber;
    private String ifsc;

    @NotBlank(message = "Payment mode is required")
    private String paymentMode; // CHEQUE / NEFT / RTGS / IMPS etc.

    private String remarks;

    private LocalDateTime transactionDateTime;

    private String remitterName;
}

