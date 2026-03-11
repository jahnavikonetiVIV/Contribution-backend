package com.jahnavi.contribution.contribution_management.controller;

import com.jahnavi.contribution.contribution_management.dto.BulkClassificationDecisionResponseDto;
import com.jahnavi.contribution.contribution_management.dto.ClassificationResponseDto;
import com.jahnavi.contribution.contribution_management.enums.Classification;
import com.jahnavi.contribution.contribution_management.service.ClassificationService;
import com.vivriti.investron.common.response.ApiResponse;
import com.vivriti.investron.common.utils.CoreUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contribution-management/classification")
@Tag(name = "Classification Management", description = "APIs for classification screen and improper worklist")
public class ClassificationController {

    private final ClassificationService classificationService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> getClassificationList(
            @RequestParam(required = false) String classification,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String vaPrefix,
            HttpServletRequest httpServletRequest) {
        log.info("Received request to get classification list with filters - classification: {}, startDate: {}, endDate: {}, vaPrefix: {}", 
                classification, startDate, endDate, vaPrefix);
        
        Optional<Classification> classificationEnum = Classification.from(classification);
        Optional<LocalDateTime> startDateTime = parseStartDateTime(startDate);
        Optional<LocalDateTime> endDateTime = parseEndDateTime(endDate);
        Optional<String> vaPrefixOpt = Optional.ofNullable(vaPrefix != null && !vaPrefix.trim().isEmpty() ? vaPrefix.trim() : null);
        
        List<ClassificationResponseDto> classificationList = classificationService.getClassificationListByFilter(
                classificationEnum, startDateTime, endDateTime, vaPrefixOpt);
        
        String message = classificationEnum
                .map(c -> String.format("%s transactions fetched successfully", c.name()))
                .orElse("All transactions fetched successfully");

        if(classificationList.isEmpty()){

                return CoreUtil.buildApiResponse(
                        classificationList,
                        httpServletRequest,
                        "Fetched  Webhook or Combined Records Successfully.",
                        HttpStatus.OK.value(),
                        HttpStatus.OK.value(),
                        HttpStatus.OK,
                        HttpStatus.OK
                );

        }
        
        return CoreUtil.buildApiResponse(
                classificationList,
                httpServletRequest,
                message,
                HttpStatus.OK.value(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.OK,
                HttpStatus.BAD_REQUEST
        );
    }

    private Optional<LocalDateTime> parseStartDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return Optional.empty();
        }
        String trimmed = dateTimeStr.trim();

        // Try parsing with time first (ISO format)
        if (trimmed.contains("T")) {
            try {
                return Optional.of(LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } catch (Exception e) {
                log.warn("Failed to parse ISO datetime format: {}", trimmed);
            }
        }

        // Try ISO date format (yyyy-MM-dd)
        try {
            return Optional.of(LocalDateTime.parse(trimmed + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } catch (Exception e) {
            // Continue to try other formats
        }
        
        // Try US date formats (MM/dd/yyyy or M/d/yyyy)
        DateTimeFormatter[] usFormatters = {
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("MM/d/yyyy"),
            DateTimeFormatter.ofPattern("M/dd/yyyy")
        };
        
        for (DateTimeFormatter formatter : usFormatters) {
            try {
                return Optional.of(LocalDate.parse(trimmed, formatter).atStartOfDay());
            } catch (Exception e) {
                // Try next format
            }
        }
        
        log.warn("Failed to parse start date/time string: {}. Tried multiple formats.", dateTimeStr);
        return Optional.empty();
    }

    private Optional<LocalDateTime> parseEndDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return Optional.empty();
        }
        String trimmed = dateTimeStr.trim();

        if (trimmed.contains("T")) {
            try {
                return Optional.of(LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } catch (Exception e) {
                log.warn("Failed to parse ISO datetime format: {}", trimmed);
            }
        }
        

        try {
            return Optional.of(LocalDateTime.parse(trimmed + "T23:59:59", DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } catch (Exception e) {
            // Continue to try other formats
        }

        DateTimeFormatter[] usFormatters = {
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("MM/d/yyyy"),
            DateTimeFormatter.ofPattern("M/dd/yyyy")
        };
        
        for (DateTimeFormatter formatter : usFormatters) {
            try {
                return Optional.of(LocalDate.parse(trimmed, formatter).atTime(23, 59, 59));
            } catch (Exception e) {
                // Try next format
            }
        }
        
        log.warn("Failed to parse end date/time string: {}. Tried multiple formats.", dateTimeStr);
        return Optional.empty();
    }


    @GetMapping("/download")
    @Operation(summary = "Download classification report", 
               description = "Downloads Excel file with classification data including UTR, Mapping Classification, Amount Mapped as Contribution, Date Time, Reason, Folio Number, Bank Account Number, IFSC, and Payment Mode")
    public void downloadClassificationReport(HttpServletResponse response) throws IOException {
        log.info("Received request to download classification report");
        
        classificationService.downloadClassificationReport(response);
    }

    @PostMapping(value="/bulk-upload-decision",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> bulkUploadClassificationDecision(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpServletRequest) {
        log.info("Received request for bulk classification decision upload");
        
        BulkClassificationDecisionResponseDto result = classificationService.bulkUploadClassificationDecision(file);
        
        return CoreUtil.buildApiResponse(
                result,
                httpServletRequest,
                result.getMessage(),
                HttpStatus.OK.value(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.OK,
                HttpStatus.BAD_REQUEST
        );
    }
}
