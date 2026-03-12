package com.jahnavi.contribution.contribution_management.controller;

import com.jahnavi.contribution.dto.ApiResponse;
import com.jahnavi.contribution.security.UserSessionUtil;
import com.jahnavi.contribution.util.CoreUtil;
import com.jahnavi.contribution.contribution_management.dto.AutoTagRequest;
import com.jahnavi.contribution.contribution_management.dto.AutoTagResponse;
import com.jahnavi.contribution.contribution_management.dto.BulkDdnMappingResponse;
import com.jahnavi.contribution.contribution_management.dto.DdnMappingFilterRequest;
import com.jahnavi.contribution.contribution_management.dto.DdnMappingHistoryDto;
import com.jahnavi.contribution.contribution_management.dto.DdnMappingManualRequest;
import com.jahnavi.contribution.contribution_management.dto.DdnMappingResponse;
import com.jahnavi.contribution.contribution_management.dto.TransactionAmountRequest;
import com.jahnavi.contribution.contribution_management.dto.TransactionAmountResponse;
import com.jahnavi.contribution.contribution_management.service.DdnMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contribution-management/ddn-mappings")
@Tag(name = "DDN Contribution Mapping", description = "APIs for auto-tagging, manual, and bulk mapping of contributions to DDNs")
public class DdnMappingController {

    private final DdnMappingService ddnMappingService;

    @PostMapping("/auto-tag")
    @Operation(summary = "Auto-tag contributions to DDNs", description = "Automatically tags approved receipts to matching DDNs within due date window")
    public ResponseEntity<ApiResponse> autoTagContributions(@RequestBody(required = false) AutoTagRequest request) {
        log.info("Received request to auto-tag contributions");

        if (request == null) {
            request = new AutoTagRequest();
        }

        AutoTagResponse response = ddnMappingService.autoTagContributions(request);

        String message = String.format("Auto-tagging completed. Success: %d, Failed: %d", response.getSuccessCount(), response.getFailedCount());
        return CoreUtil.buildApiResponse(response, false, message, HttpStatus.OK);
    }

    @GetMapping("/auto-tagged")
    @Operation(summary = "Get auto-tagged contributions", description = "Lists all auto-tagged contributions for the Auto-Tagged tab")
    public ResponseEntity<ApiResponse> getAutoTaggedContributions() {
        log.info("Fetching auto-tagged contributions");

        List<DdnMappingResponse> mappings = ddnMappingService.getAutoTaggedContributions();

        return CoreUtil.buildApiResponse(mappings, false, "Auto-tagged contributions fetched successfully", HttpStatus.OK);
    }

    @GetMapping("/manual")
    @Operation(summary = "Get manual mappings", description = "Lists all manual mappings where mappingType is MANUAL")
    public ResponseEntity<ApiResponse> getManualMappings() {
        log.info("Fetching manual mappings");

        List<DdnMappingResponse> mappings = ddnMappingService.getManualMappings();

        return CoreUtil.buildApiResponse(mappings, false, 
                String.format("Found %d manual mappings", mappings.size()), 
                HttpStatus.OK);
    }


    @PostMapping("/manual")
    @Operation(summary = "Create manual mapping", description = "Manually map a single approved transaction to DDN(s) with absolute split")
    public ResponseEntity<ApiResponse> createManualMapping(@Valid @RequestBody DdnMappingManualRequest request) {

        log.info("Creating manual mapping for UTR: {}", request.getUtr());

        String userEmail = UserSessionUtil.getUserEmail();
        DdnMappingResponse response = ddnMappingService.createManualMapping(request, userEmail);
        return CoreUtil.buildApiResponse(response, false, "Manual mapping created successfully", HttpStatus.CREATED);

    }

    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Bulk upload DDN mappings", description = "Upload CSV/XLSX file to map multiple contributions to DDNs. Maximum file size: 5MB")
    public ResponseEntity<ApiResponse> bulkUploadMappings(@RequestParam("file") MultipartFile file) {
        log.info("Processing bulk DDN mapping upload. Filename: {}", file.getOriginalFilename());

        String userEmail = getCurrentUserEmail();
        BulkDdnMappingResponse response = ddnMappingService.bulkUploadMappings(file, userEmail);

        boolean hasErrors = response.getFailedCount() > 0;
        HttpStatus status = hasErrors ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;
        return CoreUtil.buildApiResponse(response, hasErrors, response.getMessage(), status);
    }

    @GetMapping("/bulk/template")
    @Operation(summary = "Download bulk mapping template", description = "Downloads sample Excel template for bulk DDN mapping")
    public void downloadBulkMappingTemplate(HttpServletResponse response) throws IOException {
        log.info("Downloading bulk mapping template");
        ddnMappingService.downloadBulkMappingTemplate(response);
    }

    @GetMapping("/history")
    @Operation(summary = "Get mapping history", description = "Retrieves paginated mapping history with filters")
    public ResponseEntity<ApiResponse> getMappingHistory(
            @RequestParam(required = false) String utr,
            @RequestParam(required = false) String masterVa,
            @RequestParam(required = false) String vaAccount,
            @RequestParam(required = false) String folio,
            @RequestParam(required = false) Long fundId,
            @RequestParam(required = false) List<String> mappingSources,
            @RequestParam(required = false) List<String> transactionSources,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "mappedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Fetching mapping history with filters");

        DdnMappingFilterRequest filterRequest = DdnMappingFilterRequest.builder()
                .utr(utr)
                .masterVa(masterVa)
                .vaAccount(vaAccount)
                .folio(folio)
                .fundId(fundId)
                .mappingSources(mappingSources)
                .transactionSources(transactionSources)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        Page<DdnMappingHistoryDto> history = ddnMappingService.getMappingHistory(filterRequest);

        return CoreUtil.buildApiResponse(history, false, "Mapping history fetched successfully", HttpStatus.OK);
    }

    @GetMapping("/history/export")
    @Operation(summary = "Export mapping history", description = "Downloads mapping history as Excel file based on filters")
    public void exportMappingHistory(
            @RequestParam(required = false) String utr,
            @RequestParam(required = false) String masterVa,
            @RequestParam(required = false) String vaAccount,
            @RequestParam(required = false) String folio,
            @RequestParam(required = false) Long fundId,
            @RequestParam(required = false) List<String> mappingSources,
            @RequestParam(required = false) List<String> transactionSources,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            HttpServletResponse response) throws IOException {

        log.info("Exporting mapping history");

        DdnMappingFilterRequest filterRequest = DdnMappingFilterRequest.builder()
                .utr(utr)
                .masterVa(masterVa)
                .vaAccount(vaAccount)
                .folio(folio)
                .fundId(fundId)
                .mappingSources(mappingSources)
                .transactionSources(transactionSources)
                .build();

        ddnMappingService.exportMappingHistory(filterRequest, response);
    }

    @PostMapping("/dropdowns")
    @Operation(summary = "Get Dropdowns by UTR and IFSC", 
               description = "Fetches transaction amount and DDN dropdown options from VirtualAccountTransaction table based on exact UTR and exact IFSC code match. Both must match exactly. " +
                             "Each DDN option includes both individual fields (ddnId, calculationId) and a combined compositeId field (format: ddnId|calculationId) " +
                             "that can be used as the dropdown value. The composite key (documentId + calculationId) uniquely identifies each DDN.")
    public ResponseEntity<ApiResponse> getDropdownBasedOnUtrAndIfsc(
            @Valid @RequestBody TransactionAmountRequest request) {
        
        log.info("Fetching transaction amount for UTR: {} and IFSC: {}", request.getUtr(), request.getIfscCode());

        TransactionAmountResponse response = ddnMappingService.getDropdownBasedOnUtrAndIfsc(request);

        String message = "Transaction details and DDN dropdowns fetched successfully for UTR: " + request.getUtr();

        return CoreUtil.buildApiResponse(response, false, message, HttpStatus.OK);
    }

    /**
     * Helper method to get current authenticated user's email
     */
    private String getCurrentUserEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName(); // Usually the email or username
            }
        } catch (Exception e) {
            log.warn("Could not retrieve authenticated user email: {}", e.getMessage());
        }
        return "system@investron.com"; // Fallback
    }
}

