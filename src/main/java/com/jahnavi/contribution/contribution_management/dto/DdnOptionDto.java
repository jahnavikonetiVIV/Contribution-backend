package com.jahnavi.contribution.contribution_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DdnOptionDto {
    private String ddnId; // Document ID from DdnDocument (composite key part 1)
    private Long calculationId; // Calculation ID from DdnDocument (composite key part 2)
    private String compositeId; // Combined unique identifier: ddnId|calculationId (for dropdown value)
    private String folio;
    private String fundName;
    private Long fundId;
    private BigDecimal totalPayableAmount;
    private LocalDate payableFromDate;
    private LocalDate payableToDate;
    private String status;
    private String commitmentType; // Initial, Top-up, Secondary Transfer
    private BigDecimal transactionAmount; // Transaction amount from VirtualAccountTransaction based on UTR and IFSC
    private String utr; // UTR from mapping
    private String ifscCode; // IFSC code from mapping
}

