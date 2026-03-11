package com.jahnavi.contribution.contribution_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for filtering MIS download records
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MisDownloadRequest {

    private String utr;
    private String investorReference;
    private String fundName;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private String source; // EMAIL, WEBHOOK, ALL
}




