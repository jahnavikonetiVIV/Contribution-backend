package com.jahnavi.contribution.contribution_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundRequestDropdownOptionsResponse {
    private List<FundRequestOptionDto> initialCommitmentFundRequests;
    private List<FundRequestOptionDto> topupFundRequests;
    private List<FundRequestOptionDto> secondaryTransferFundRequests;
}
