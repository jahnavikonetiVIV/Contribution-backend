package com.jahnavi.contribution.contribution_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolioWithoutVaDto {
    private String folio;
    private String investorApplicationNo;
    private String fundName;
    private Long fundId;
    private String onboardingType;
    private String eSignStatus;
    private Boolean eligibleForVa; // true if eligible based on conditions
}

