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
public class DdnDropdownOptionsResponse {
    private List<DdnOptionDto> initialCommitmentDdns;
    private List<DdnOptionDto> topupDdns;
    private List<DdnOptionDto> secondaryTransferDdns;
}



