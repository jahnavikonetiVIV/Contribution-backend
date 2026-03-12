package com.jahnavi.contribution.contribution_management.controller;

import com.jahnavi.contribution.dto.ApiResponse;
import com.jahnavi.contribution.util.CoreUtil;
import com.jahnavi.contribution.contribution_management.service.impl.MisProcessingServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contribution-management/ecollect-mis")
@Tag(name = "Ecollect MIS Report", description = "APIs for manually uploading and processing Ecollect MIS report files")
public class EcollectMisReportController {

    private final MisProcessingServiceImpl misProcessingServiceImpl;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload MIS report file manually", 
               description = "Manually upload an Excel file (.xlsx, .xls) for MIS report processing. This endpoint is for testing purposes.")
    public ResponseEntity<ApiResponse> uploadMisReportFile(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpServletRequest) {
        
        log.info("Received manual upload request for MIS report file: {}", file.getOriginalFilename());
        
        try {
            // Validate file
            if (file.isEmpty()) {
                return CoreUtil.buildApiResponse(
                    null, 
                    httpServletRequest, 
                    "File is empty", 
                    HttpStatus.BAD_REQUEST.value(), 
                    HttpStatus.BAD_REQUEST.value(), 
                    HttpStatus.BAD_REQUEST, 
                    HttpStatus.BAD_REQUEST
                );
            }
            
            String fileName = file.getOriginalFilename();
            if (fileName == null || 
                (!fileName.toLowerCase().endsWith(".xlsx") && 
                 !fileName.toLowerCase().endsWith(".xls"))) {
                return CoreUtil.buildApiResponse(
                    null, 
                    httpServletRequest, 
                    "Invalid file type. Only Excel files (.xlsx, .xls) are allowed", 
                    HttpStatus.BAD_REQUEST.value(), 
                    HttpStatus.BAD_REQUEST.value(), 
                    HttpStatus.BAD_REQUEST, 
                    HttpStatus.BAD_REQUEST
                );
            }
            
            // Generate test values for messageId and s3Path
            Long testMessageId = null;
            String testS3Path = "test/manual-upload/" + fileName;
            
            log.info("Processing MIS report file: {} with test messageId: {}", fileName, testMessageId);
            
            // Process the file
            misProcessingServiceImpl.processMisReportFile(file, testMessageId, testS3Path, null);
            
            log.info("Successfully processed MIS report file: {}", fileName);
            
            return CoreUtil.buildApiResponse(
                "File processed successfully", 
                httpServletRequest, 
                "MIS report file uploaded and processed successfully", 
                HttpStatus.OK.value(), 
                HttpStatus.BAD_REQUEST.value(), 
                HttpStatus.OK, 
                HttpStatus.BAD_REQUEST
            );
            
        } catch (Exception e) {
            log.error("Error processing MIS report file: {}", e.getMessage(), e);
            return CoreUtil.buildApiResponse(
                null, 
                httpServletRequest, 
                "Failed to process MIS report file: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                HttpStatus.INTERNAL_SERVER_ERROR, 
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}

