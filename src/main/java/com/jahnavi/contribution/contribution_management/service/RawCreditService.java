package com.jahnavi.contribution.contribution_management.service;

import com.jahnavi.contribution.contribution_management.dto.IngestionResponseDto;
import com.jahnavi.contribution.contribution_management.dto.MisIngestionDto;

import java.io.IOException;
import java.util.List;

public interface RawCreditService {

    List<IngestionResponseDto> fetchWebhookOrCombinedRecords(String classification, String search);

    byte[] fetchRecordsForDownload(String classification) throws IOException;

    List<MisIngestionDto> fetchMisRecords();
}
