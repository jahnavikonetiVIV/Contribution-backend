package com.jahnavi.contribution.contribution_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkClassificationDecisionResponseDto {
    private int totalRecords;
    private int successCount;
    private int failedCount;
    private String message;
    private List<BulkClassificationErrorDto> errors;
}
