package com.jahnavi.contribution.contribution_management.controller;

import com.jahnavi.contribution.contribution_management.dto.IngestionResponseDto;
import com.jahnavi.contribution.contribution_management.dto.MisIngestionDto;
import com.jahnavi.contribution.contribution_management.service.RawCreditService;
import com.vivriti.investron.common.response.ApiResponse;
import com.vivriti.investron.common.utils.CoreUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/contribution-management/ingestion")
public class IngestionController {

    private final RawCreditService rawCreditService;

    @GetMapping("/fetchRecords")
    public ResponseEntity<ApiResponse> fetchData(
            @RequestParam @NotBlank String classification,
            @RequestParam(required = false) String search,
            HttpServletRequest httpServletRequest) {
        List<IngestionResponseDto> fetchedRecords = rawCreditService.fetchWebhookOrCombinedRecords(classification, search);

        if (fetchedRecords.isEmpty()) {
            return CoreUtil.buildApiResponse(
                    fetchedRecords,
                    httpServletRequest,
                    "Fetched  Webhook or Combined Records Successfully.",
                    HttpStatus.OK.value(),
                    HttpStatus.OK.value(),
                    HttpStatus.OK,
                    HttpStatus.OK
            );
        }
        return CoreUtil.buildApiResponse(
                fetchedRecords,
                httpServletRequest,
                "Ingestion Records fetched Successfully.",
                HttpStatus.OK.value(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.OK,
                HttpStatus.BAD_REQUEST
        );
    }

    @GetMapping("/downloadIngestionRecords")
    public ResponseEntity<byte[]> downloadIngestionRecords(
            @RequestParam String classification) throws IOException {

        byte[] file = rawCreditService.fetchRecordsForDownload(classification);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=ingestion-records.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    @GetMapping("/fetchMisRecords")
    ResponseEntity<ApiResponse> fetchMisRecords(HttpServletRequest httpServletRequest){
        List<MisIngestionDto> fetchedRecords = rawCreditService.fetchMisRecords();

        if (fetchedRecords.isEmpty()) {
            return CoreUtil.buildApiResponse(
                    fetchedRecords,
                    httpServletRequest,
                    "Fetched MIS Records Successfully.",
                    HttpStatus.OK.value(),
                    HttpStatus.OK.value(),
                    HttpStatus.OK,
                    HttpStatus.OK
            );
        }
        return CoreUtil.buildApiResponse(
                fetchedRecords,
                httpServletRequest,
                "Ingestion Records fetched Successfully.",
                HttpStatus.OK.value(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.OK,
                HttpStatus.BAD_REQUEST
        );
    }


}
