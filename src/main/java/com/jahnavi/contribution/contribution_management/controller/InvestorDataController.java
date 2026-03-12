package com.jahnavi.contribution.contribution_management.controller;

import com.jahnavi.contribution.contribution_management.dto.InvestorDataDto;
import com.jahnavi.contribution.contribution_management.entity.InvestorData;
import com.jahnavi.contribution.contribution_management.repository.InvestorDataRepository;
import com.jahnavi.contribution.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contribution-management/investor-data")
@Tag(name = "Investor Data", description = "APIs for managing investor bank and virtual account data")
public class InvestorDataController {

    private final InvestorDataRepository investorDataRepository;

    @GetMapping
    @Operation(summary = "List all investor data")
    public ResponseEntity<ApiResponse> listAll() {
        List<InvestorDataDto> data = investorDataRepository.findAll().stream()
                .filter(i -> Boolean.TRUE.equals(i.getActive()))
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.builder().data(data).build());
    }

    @PostMapping
    @Operation(summary = "Create investor data")
    public ResponseEntity<ApiResponse> create(@RequestBody InvestorDataDto dto) {
        InvestorData entity = InvestorData.builder()
                .investorName(dto.getInvestorName())
                .virtualAccountNumber(dto.getVirtualAccountNumber())
                .bankAccountNumber(dto.getBankAccountNumber())
                .ifscCode(dto.getIfscCode())
                .build();
        entity = investorDataRepository.save(entity);
        return ResponseEntity.ok(ApiResponse.builder().data(toDto(entity)).build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update investor data")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @RequestBody InvestorDataDto dto) {
        InvestorData entity = investorDataRepository.findById(id)
                .orElseThrow(() -> new com.jahnavi.contribution.exception.CoreException(404, "Investor data not found"));
        entity.setInvestorName(dto.getInvestorName());
        entity.setVirtualAccountNumber(dto.getVirtualAccountNumber());
        entity.setBankAccountNumber(dto.getBankAccountNumber());
        entity.setIfscCode(dto.getIfscCode());
        entity = investorDataRepository.save(entity);
        return ResponseEntity.ok(ApiResponse.builder().data(toDto(entity)).build());
    }

    private InvestorDataDto toDto(InvestorData e) {
        return InvestorDataDto.builder()
                .id(e.getId())
                .investorName(e.getInvestorName())
                .virtualAccountNumber(e.getVirtualAccountNumber())
                .bankAccountNumber(e.getBankAccountNumber())
                .ifscCode(e.getIfscCode())
                .build();
    }
}
