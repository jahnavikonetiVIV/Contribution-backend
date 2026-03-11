package com.jahnavi.contribution.contribution_management.service;

import com.jahnavi.contribution.contribution_management.dto.BulkClassificationDecisionResponseDto;
import com.jahnavi.contribution.contribution_management.dto.ClassificationResponseDto;
import com.jahnavi.contribution.contribution_management.enums.Classification;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClassificationService {



    List<ClassificationResponseDto> getClassificationListByFilter(
            Optional<Classification> classification,
            Optional<LocalDateTime> startDate,
            Optional<LocalDateTime> endDate,
            Optional<String> vaPrefix
    );

    void downloadClassificationReport(HttpServletResponse response) throws IOException;

    BulkClassificationDecisionResponseDto bulkUploadClassificationDecision(MultipartFile file);
}
