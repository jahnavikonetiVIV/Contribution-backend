package com.jahnavi.contribution.contribution_management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContributionClassificationDecisionRequest {

    @NotBlank(message = "UTR is required")
    private String utr;

    /**
     * Classification decided by user (Proper/Rejected/Improper).
     * For requirement: Proper or Rejected will be provided in bulk file.
     */
    @NotBlank(message = "Classification is required")
    private String classification;

    /**
     * Optional state override. If classification = Proper => state will be set to User-Approved.
     * If classification = Rejected and current state is Hold and requested state is Returned => set Returned.
     * Otherwise classification Rejected => state Hold.
     */
    private String state;

    /**
     * Folio is needed for Proper decisions.
     */
    private String folioNumber;
}

