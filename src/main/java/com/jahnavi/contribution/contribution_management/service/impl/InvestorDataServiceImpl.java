package com.jahnavi.contribution.contribution_management.service.impl;

import com.jahnavi.contribution.contribution_management.entity.InvestorData;
import com.jahnavi.contribution.contribution_management.repository.InvestorDataRepository;
import com.jahnavi.contribution.contribution_management.service.InvestorDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestorDataServiceImpl implements InvestorDataService {

    private final InvestorDataRepository investorDataRepository;

    @Override
    public boolean matchesRegisteredBank(String virtualAccountNumber, String bankAccountNumber, String ifscCode) {
        if (virtualAccountNumber == null || bankAccountNumber == null || bankAccountNumber.isBlank()) {
            return false;
        }
        Optional<InvestorData> investorOpt = investorDataRepository.findByVirtualAccountNumberAndActiveTrue(virtualAccountNumber);
        if (investorOpt.isEmpty()) {
            return false;
        }
        InvestorData investor = investorOpt.get();
        return normalize(investor.getBankAccountNumber()).equals(normalize(bankAccountNumber))
                && normalize(investor.getIfscCode()).equals(normalize(ifscCode));
    }

    @Override
    public boolean isRegisteredVirtualAccount(String virtualAccountNumber) {
        if (virtualAccountNumber == null || virtualAccountNumber.isBlank()) {
            return false;
        }
        return investorDataRepository.findByVirtualAccountNumberAndActiveTrue(virtualAccountNumber).isPresent();
    }

    @Override
    public Optional<String> getBankAccountNumber(String virtualAccountNumber) {
        return investorDataRepository.findByVirtualAccountNumberAndActiveTrue(virtualAccountNumber)
                .map(InvestorData::getBankAccountNumber);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ENGLISH);
    }
}
