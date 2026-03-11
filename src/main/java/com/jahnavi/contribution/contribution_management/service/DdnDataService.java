package com.jahnavi.contribution.contribution_management.service;

import com.jahnavi.contribution.contribution_management.dto.DdnOptionDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Service for DDN (Drawdown Notice) data from the ddn table.
 * Used for DDN contribution mapping.
 */
public interface DdnDataService {

    /**
     * Get open DDN options for an investor (by folio or investor id).
     * Returns DDNs with status OPEN where total paid is less than total payable.
     */
    List<DdnOptionDto> getOpenDdnOptions(String investorId, Function<String, BigDecimal> totalPaidProvider);

    /**
     * Find DDN by its unique code.
     */
    Optional<DdnDefinition> findDdn(String ddnCode);

    /**
     * Get current status (OPEN/CLOSE) for a DDN based on amount paid.
     */
    String currentStatus(String ddnCode, Function<String, BigDecimal> totalPaidProvider);

    /**
     * Check if the service has DDN data for the given investor.
     */
    boolean hasDdnDataForInvestor(String investorId);

    record DdnDefinition(String ddnCode, String commitmentType, BigDecimal totalPayableAmount, Long fundId, String fundName) {
    }
}
