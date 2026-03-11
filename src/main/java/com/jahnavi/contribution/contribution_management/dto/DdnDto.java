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
public class DdnDto {
    private Long id;
    private String ddnCode;
    private String fundName;
    private Long fundId;
    private String investorId;
    private BigDecimal totalPayableAmount;
    private String status; // OPEN or CLOSE
    private String commitmentType; // Initial, Top-up
}
