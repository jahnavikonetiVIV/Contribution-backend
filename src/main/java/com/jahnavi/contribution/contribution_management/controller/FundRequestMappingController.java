package com.jahnavi.contribution.contribution_management.controller;

import com.jahnavi.contribution.dto.ApiResponse;
import com.jahnavi.contribution.security.UserSessionUtil;
import com.jahnavi.contribution.util.CoreUtil;
import com.jahnavi.contribution.contribution_management.dto.AutoTagRequest;
import com.jahnavi.contribution.contribution_management.dto.AutoTagResponse;
import com.jahnavi.contribution.contribution_management.dto.BulkFundRequestMappingResponse;
import com.jahnavi.contribution.contribution_management.dto.FundRequestMappingFilterRequest;
import com.jahnavi.contribution.contribution_management.dto.FundRequestMappingHistoryDto;
import com.jahnavi.contribution.contribution_management.dto.FundRequestMappingManualRequest;
import com.jahnavi.contribution.contribution_management.dto.FundRequestMappingResponse;
import com.jahnavi.contribution.contribution_management.dto.TransactionAmountRequest;
import com.jahnavi.contribution.contribution_management.dto.TransactionAmountResponse;
import com.jahnavi.contribution.contribution_management.service.FundRequestMappingService;
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
@RequestMapping("/api/v1/contribution-management/fund-request-mappings")
@Tag(name = "Fund Request Contribution Mapping", description = "APIs for auto-tagging, manual, and bulk mapping of contributions to Fund Requests")
public class FundRequestMappingController {

    private final FundRequestMappingService fundRequestMappingService;

    @PostMapping("/auto-tag")
    @Operation(summary = "Auto-tag contributions to Fund Requests", description = "Automatically tags approved receipts to matching Fund Requests within due date window")
    public ResponseEntity<ApiResponse> autoTagContributions(@RequestBody(required = false) AutoTagRequest request) {
        log.info("Received request to auto-tag contributions");

        if (request == null) {
            request = new AutoTagRequest();
        }

        AutoTagResponse response = fundRequestMappingService.autoTagContributions(request);

        String message = String.format("Auto-tagging completed. Success: %d, Failed: %d", response.getSuccessCount(), response.getFailedCount());
        return CoreUtil.buildApiResponse(response, false, message, HttpStatus.OK);
    }

    @GetMapping("/auto-tagged")
    @Operation(summary = "Get auto-tagged contributions", description = "Lists all auto-tagged contributions for the Auto-Tagged tab")
    public ResponseEntity<ApiResponse> getAutoTaggedContributions() {
        log.info("Fetching auto-tagged contributions");

        List<FundRequestMappingResponse> mappings = fundRequestMappingService.getAutoTaggedContributions();

        return CoreUtil.buildApiResponse(mappings, false, "Auto-tagged contributions fetched successfully", HttpStatus.OK);
    }

    @GetMapping("/manual")
    @Operation(summary = "Get manual mappings", description = "Lists all manual mappings where mappingType is MANUAL")
    public ResponseEntity<ApiResponse> getManualMappings() {
        log.info("Fetching manual mappings");

        List<FundRequestMappingResponse> mappings = fundRequestMappingService.getManualMappings();

        return CoreUtil.buildApiResponse(mappings, false,
                String.format("Found %d manual mappings", mappings.size()),
                HttpStatus.OK);
    }

    @PostMapping("/manual")
    @Operation(summary = "Create manual mapping", description = "Manually map a single approved transaction to Fund Request(s) with absolute split")
    public ResponseEntity<ApiResponse> createManualMapping(@Valid @RequestBody FundRequestMappingManualRequest request) {

        log.info("Creating manual mapping for UTR: {}", request.getUtr());

        String userEmail = UserSessionUtil.getUserEmail();
        FundRequestMappingResponse response = fundRequestMappingService.createManualMapping(request, userEmail);
        return CoreUtil.buildApiResponse(response, false, "Manual mapping created successfully", HttpStatus.CREATED);
    }

    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Bulk upload Fund Request mappings", description = "Upload CSV/XLSX file to map multiple contributions to Fund Requests. Maximum file size: 5MB")
    public ResponseEntity<ApiResponse> bulkUploadMappings(@RequestParam("file") MultipartFile file) {
        log.info("Processing bulk Fund Request mapping upload. Filename: {}", file.getOriginalFilename());

        String userEmail = getCurrentUserEmail();
        BulkFundRequestMappingResponse response = fundRequestMappingService.bulkUploadMappings(file, userEmail);

        boolean hasErrors = response.getFailedCount() > 0;
        HttpStatus status = hasErrors ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;
        return CoreUtil.buildApiResponse(response, hasErrors, response.getMessage(), status);
    }

    @GetMapping("/bulk/template")
    @Operation(summary = "Download bulk mapping template", description = "Downloads sample Excel template for bulk Fund Request mapping")
    public void downloadBulkMappingTemplate(HttpServletResponse response) throws IOException {
        log.info("Downloading bulk mapping template");
        fundRequestMappingService.downloadBulkMappingTemplate(response);
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

        FundRequestMappingFilterRequest filterRequest = FundRequestMappingFilterRequest.builder()
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

        Page<FundRequestMappingHistoryDto> history = fundRequestMappingService.getMappingHistory(filterRequest);

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

        FundRequestMappingFilterRequest filterRequest = FundRequestMappingFilterRequest.builder()
                .utr(utr)
                .masterVa(masterVa)
                .vaAccount(vaAccount)
                .folio(folio)
                .fundId(fundId)
                .mappingSources(mappingSources)
                .transactionSources(transactionSources)
                .build();

        fundRequestMappingService.exportMappingHistory(filterRequest, response);
    }

    @PostMapping("/dropdowns")
    @Operation(summary = "Get Dropdowns by UTR and IFSC",
            description = "Fetches transaction amount and Fund Request dropdown options from VirtualAccountTransaction table based on exact UTR and exact IFSC code match. Both must match exactly. " +
                    "Each Fund Request option includes fundRequestId, calculationId and compositeId for dropdown value.")
    public ResponseEntity<ApiResponse> getDropdownBasedOnUtrAndIfsc(
            @Valid @RequestBody TransactionAmountRequest request) {

        log.info("Fetching transaction amount for UTR: {} and IFSC: {}", request.getUtr(), request.getIfscCode());

        TransactionAmountResponse response = fundRequestMappingService.getDropdownBasedOnUtrAndIfsc(request);

        String message = "Transaction details and Fund Request dropdowns fetched successfully for UTR: " + request.getUtr();

        return CoreUtil.buildApiResponse(response, false, message, HttpStatus.OK);
    }

    private String getCurrentUserEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.warn("Could not retrieve authenticated user email: {}", e.getMessage());
        }
        return "system@investron.com";
    }
}
