package com.jahnavi.contribution.contribution_management.service;

import com.jahnavi.contribution.contribution_management.dto.AutoTagRequest;
import com.jahnavi.contribution.contribution_management.dto.AutoTagResponse;
import com.jahnavi.contribution.contribution_management.dto.BulkFundRequestMappingResponse;
import com.jahnavi.contribution.contribution_management.dto.FundRequestMappingFilterRequest;
import com.jahnavi.contribution.contribution_management.dto.FundRequestMappingHistoryDto;
import com.jahnavi.contribution.contribution_management.dto.FundRequestMappingManualRequest;
import com.jahnavi.contribution.contribution_management.dto.FundRequestMappingResponse;
import com.jahnavi.contribution.contribution_management.dto.TransactionAmountRequest;
import com.jahnavi.contribution.contribution_management.dto.TransactionAmountResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FundRequestMappingService {

    AutoTagResponse autoTagContributions(AutoTagRequest request);

    List<FundRequestMappingResponse> getAutoTaggedContributions();

    FundRequestMappingResponse createManualMapping(FundRequestMappingManualRequest request, String userEmail);

    BulkFundRequestMappingResponse bulkUploadMappings(MultipartFile file, String userEmail);

    void downloadBulkMappingTemplate(HttpServletResponse response) throws IOException;

    Page<FundRequestMappingHistoryDto> getMappingHistory(FundRequestMappingFilterRequest filterRequest);

    void exportMappingHistory(FundRequestMappingFilterRequest filterRequest, HttpServletResponse response) throws IOException;

    List<FundRequestMappingResponse> getManualMappings();

    TransactionAmountResponse getDropdownBasedOnUtrAndIfsc(TransactionAmountRequest request);
}
