package com.jahnavi.contribution.contribution_management.service;

import com.jahnavi.contribution.contribution_management.dto.FundRequestOptionDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Service for Fund Request data from the fund_request table.
 */
public interface FundRequestDataService {

    List<FundRequestOptionDto> getOpenFundRequestOptions(String investorId, Function<String, BigDecimal> totalPaidProvider);

    Optional<FundRequestDefinition> findFundRequest(String fundRequestCode);

    String currentStatus(String fundRequestCode, Function<String, BigDecimal> totalPaidProvider);

    boolean hasFundRequestDataForInvestor(String investorId);

    record FundRequestDefinition(String fundRequestCode, String commitmentType, BigDecimal totalPayableAmount, Long fundId, String fundName) {
    }
}
