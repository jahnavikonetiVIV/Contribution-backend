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
public class ClassificationResponseDto {
    private String utr;
    private String va;
    private String folio;
    private BigDecimal amount;
    private String classification;
    private String reason;
    private String bankAccount;
    private String ifsc;
    private String paymentMode;
    private LocalDateTime dateAndTime;
}
