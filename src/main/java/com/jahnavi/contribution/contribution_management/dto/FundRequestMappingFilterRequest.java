package com.jahnavi.contribution.contribution_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundRequestMappingFilterRequest {
    private String utr;
    private String masterVa;
    private String vaAccount;
    private String folio;
    private Long fundId;
    private List<String> mappingSources;
    private List<String> transactionSources;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}
