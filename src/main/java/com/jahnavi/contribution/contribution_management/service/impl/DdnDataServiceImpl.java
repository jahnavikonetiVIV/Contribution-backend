package com.jahnavi.contribution.contribution_management.service.impl;

import com.jahnavi.contribution.contribution_management.dto.DdnOptionDto;
import com.jahnavi.contribution.contribution_management.entity.Ddn;
import com.jahnavi.contribution.contribution_management.repository.DdnRepository;
import com.jahnavi.contribution.contribution_management.service.DdnDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class DdnDataServiceImpl implements DdnDataService {

    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_CLOSED = "CLOSE";

    private final DdnRepository ddnRepository;

    @Override
    public List<DdnOptionDto> getOpenDdnOptions(String folio, Function<String, BigDecimal> totalPaidProvider) {
        List<Ddn> ddns = ddnRepository.findByInvestorFolioAndStatusAndActiveTrueWithInvestor(folio, STATUS_OPEN);
        return ddns.stream()
                .filter(ddn -> STATUS_OPEN.equals(currentStatus(ddn.getDdnCode(), totalPaidProvider)))
                .map(this::toDdnOptionDto)
                .toList();
    }

    @Override
    public Optional<DdnDefinition> findDdn(String ddnCode) {
        return ddnRepository.findByDdnCodeAndActiveTrue(normalize(ddnCode))
                .map(d -> new DdnDefinition(
                        d.getDdnCode(),
                        d.getCommitmentType() != null ? d.getCommitmentType() : "Initial",
                        d.getTotalPayableAmount(),
                        d.getFundId(),
                        d.getFundName()
                ));
    }

    @Override
    public String currentStatus(String ddnCode, Function<String, BigDecimal> totalPaidProvider) {
        Optional<Ddn> ddnOpt = ddnRepository.findByDdnCodeAndActiveTrue(normalize(ddnCode));
        if (ddnOpt.isEmpty()) {
            return "UNKNOWN";
        }
        Ddn ddn = ddnOpt.get();
        if (STATUS_CLOSED.equalsIgnoreCase(ddn.getStatus()) || "CLOSED".equalsIgnoreCase(ddn.getStatus())) {
            return "CLOSED";
        }
        BigDecimal paid = totalPaidProvider != null ? totalPaidProvider.apply(ddn.getDdnCode()) : BigDecimal.ZERO;
        BigDecimal total = ddn.getTotalPayableAmount() != null ? ddn.getTotalPayableAmount() : BigDecimal.ZERO;
        return paid.compareTo(total) >= 0 ? "CLOSED" : STATUS_OPEN;
    }

    @Override
    public boolean hasDdnDataForInvestor(String folio) {
        return !ddnRepository.findByInvestor_FolioAndStatusAndActiveTrue(folio, STATUS_OPEN).isEmpty()
                || !ddnRepository.findByInvestor_FolioAndStatusAndActiveTrue(folio, STATUS_CLOSED).isEmpty();
    }

    private DdnOptionDto toDdnOptionDto(Ddn ddn) {
        String folio = ddn.getInvestor() != null ? ddn.getInvestor().getFolio() : null;
        return DdnOptionDto.builder()
                .ddnId(ddn.getDdnCode())
                .calculationId(ddn.getId())
                .compositeId(ddn.getDdnCode())
                .folio(folio)
                .fundName(ddn.getFundName())
                .fundId(ddn.getFundId())
                .totalPayableAmount(ddn.getTotalPayableAmount())
                .status(STATUS_OPEN)
                .commitmentType(ddn.getCommitmentType() != null ? ddn.getCommitmentType() : "Initial")
                .build();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ENGLISH);
    }
}
