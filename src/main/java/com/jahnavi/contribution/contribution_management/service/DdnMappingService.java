package com.jahnavi.contribution.contribution_management.service;

import com.jahnavi.contribution.contribution_management.dto.AutoTagRequest;
import com.jahnavi.contribution.contribution_management.dto.AutoTagResponse;
import com.jahnavi.contribution.contribution_management.dto.BulkDdnMappingResponse;
import com.jahnavi.contribution.contribution_management.dto.DdnMappingFilterRequest;
import com.jahnavi.contribution.contribution_management.dto.DdnMappingHistoryDto;
import com.jahnavi.contribution.contribution_management.dto.DdnMappingManualRequest;
import com.jahnavi.contribution.contribution_management.dto.DdnMappingResponse;
import com.jahnavi.contribution.contribution_management.dto.TransactionAmountRequest;
import com.jahnavi.contribution.contribution_management.dto.TransactionAmountResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DdnMappingService {

    /**
     * Auto-tag approved receipts to matching DDNs within due date window
     */
    AutoTagResponse autoTagContributions(AutoTagRequest request);

    /**
     * Get all auto-tagged contributions for display in Auto-Tagged tab
     */
    List<DdnMappingResponse> getAutoTaggedContributions();

    /**
     * Manual single mapping of a transaction to DDN(s) with split
     */
    DdnMappingResponse createManualMapping(DdnMappingManualRequest request, String userEmail);

    /**
     * Bulk mapping via file upload
     */
    BulkDdnMappingResponse bulkUploadMappings(MultipartFile file, String userEmail);

    /**
     * Download sample template for bulk upload
     */
    void downloadBulkMappingTemplate(HttpServletResponse response) throws IOException;

    /**
     * Get mapping history with filters and pagination
     */
    Page<DdnMappingHistoryDto> getMappingHistory(DdnMappingFilterRequest filterRequest);

    /**
     * Export mapping history to Excel/CSV
     */
    void exportMappingHistory(DdnMappingFilterRequest filterRequest, HttpServletResponse response) throws IOException;

    /**
     * Get all manual mappings
     */
    List<DdnMappingResponse> getManualMappings();

    /**
     * Get transaction amount by UTR and IFSC from VirtualAccountTransaction table
     */
    TransactionAmountResponse getDropdownBasedOnUtrAndIfsc(TransactionAmountRequest request);
}

