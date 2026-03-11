package com.jahnavi.contribution.contribution_management.service;

import java.util.Optional;

/**
 * Service for investor data lookups.
 * Used for bank account verification (proper/improper classification)
 * and virtual account number verification.
 */
public interface InvestorDataService {

    /**
     * Check if the given bank account number and IFSC match a registered investor
     * for the given virtual account number.
     */
    boolean matchesRegisteredBank(String virtualAccountNumber, String bankAccountNumber, String ifscCode);

    /**
     * Verify if the virtual account number exists in investor data.
     */
    boolean isRegisteredVirtualAccount(String virtualAccountNumber);

    /**
     * Get bank account number for an investor by virtual account number.
     */
    Optional<String> getBankAccountNumber(String virtualAccountNumber);
}
