package com.jahnavi.contribution.contribution_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkClassificationDecisionRequestDto {
    private String utr;
    private String classification; // PROPER or IMPROPER
    private String folioNumber; // Mandatory when changing to PROPER
    private String ifsc; // Used to find transaction (first 4 digits)
}
