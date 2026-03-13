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
public class BulkFundRequestMappingResponse {
    private Integer totalRecords;
    private Integer processedRecords;
    private Integer successCount;
    private Integer failedCount;
    private String message;
    private List<BulkFundRequestMappingUploadRow> failedRows;
    private List<FundRequestMappingResponse> successfulMappings;
}
