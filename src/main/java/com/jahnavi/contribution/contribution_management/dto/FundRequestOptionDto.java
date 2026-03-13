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
public class FundRequestOptionDto {
    private String fundRequestId;
    private Long calculationId;
    private String compositeId;
    private String folio;
    private String fundName;
    private Long fundId;
    private BigDecimal totalPayableAmount;
    private LocalDate payableFromDate;
    private LocalDate payableToDate;
    private String status;
    private String commitmentType;
    private BigDecimal transactionAmount;
    private String utr;
    private String ifscCode;
}
