package com.jahnavi.contribution.contribution_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestorDataDto {
    private Long id;
    private String investorName;
    private String virtualAccountNumber;
    private String bankAccountNumber;
    private String ifscCode;
}
