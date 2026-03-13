package com.jahnavi.contribution.contribution_management.service.impl;

import com.jahnavi.contribution.contribution_management.dto.FundRequestOptionDto;
import com.jahnavi.contribution.contribution_management.entity.FundRequest;
import com.jahnavi.contribution.contribution_management.repository.FundRequestRepository;
import com.jahnavi.contribution.contribution_management.service.FundRequestDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class FundRequestDataServiceImpl implements FundRequestDataService {

    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_CLOSED = "CLOSE";

    private final FundRequestRepository fundRequestRepository;

    @Override
    public List<FundRequestOptionDto> getOpenFundRequestOptions(String folio, Function<String, BigDecimal> totalPaidProvider) {
        List<FundRequest> fundRequests = fundRequestRepository.findByInvestorFolioAndStatusAndActiveTrueWithInvestor(folio, STATUS_OPEN);
        return fundRequests.stream()
                .filter(fr -> STATUS_OPEN.equals(currentStatus(fr.getFundRequestCode(), totalPaidProvider)))
                .map(this::toFundRequestOptionDto)
                .toList();
    }

    @Override
    public Optional<FundRequestDefinition> findFundRequest(String fundRequestCode) {
        return fundRequestRepository.findByFundRequestCodeAndActiveTrue(normalize(fundRequestCode))
                .map(f -> new FundRequestDefinition(
                        f.getFundRequestCode(),
                        f.getCommitmentType() != null ? f.getCommitmentType() : "Initial",
                        f.getTotalPayableAmount(),
                        f.getFundId(),
                        f.getFundName()
                ));
    }

    @Override
    public String currentStatus(String fundRequestCode, Function<String, BigDecimal> totalPaidProvider) {
        Optional<FundRequest> frOpt = fundRequestRepository.findByFundRequestCodeAndActiveTrue(normalize(fundRequestCode));
        if (frOpt.isEmpty()) {
            return "UNKNOWN";
        }
        FundRequest fr = frOpt.get();
        if (STATUS_CLOSED.equalsIgnoreCase(fr.getStatus()) || "CLOSED".equalsIgnoreCase(fr.getStatus())) {
            return "CLOSED";
        }
        BigDecimal paid = totalPaidProvider != null ? totalPaidProvider.apply(fr.getFundRequestCode()) : BigDecimal.ZERO;
        BigDecimal total = fr.getTotalPayableAmount() != null ? fr.getTotalPayableAmount() : BigDecimal.ZERO;
        return paid.compareTo(total) >= 0 ? "CLOSED" : STATUS_OPEN;
    }

    @Override
    public boolean hasFundRequestDataForInvestor(String folio) {
        return !fundRequestRepository.findByInvestor_FolioAndStatusAndActiveTrue(folio, STATUS_OPEN).isEmpty()
                || !fundRequestRepository.findByInvestor_FolioAndStatusAndActiveTrue(folio, STATUS_CLOSED).isEmpty();
    }

    private FundRequestOptionDto toFundRequestOptionDto(FundRequest fr) {
        String folio = fr.getInvestor() != null ? fr.getInvestor().getFolio() : null;
        return FundRequestOptionDto.builder()
                .fundRequestId(fr.getFundRequestCode())
                .calculationId(fr.getId())
                .compositeId(fr.getFundRequestCode())
                .folio(folio)
                .fundName(fr.getFundName())
                .fundId(fr.getFundId())
                .totalPayableAmount(fr.getTotalPayableAmount())
                .status(STATUS_OPEN)
                .commitmentType(fr.getCommitmentType() != null ? fr.getCommitmentType() : "Initial")
                .build();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ENGLISH);
    }
}
