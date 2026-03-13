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
public class AutoTagResultDto {
    private String utr;
    private String folio;
    private String fundRequestId;
    private BigDecimal amount;
    private String status;
    private String message;
}

