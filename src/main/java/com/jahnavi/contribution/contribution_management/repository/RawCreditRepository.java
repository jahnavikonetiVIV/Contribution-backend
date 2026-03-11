package com.jahnavi.contribution.contribution_management.repository;

import com.jahnavi.contribution.contribution_management.entity.RawCreditCombined;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RawCreditRepository extends JpaRepository<RawCreditCombined, Long>, JpaSpecificationExecutor<RawCreditCombined> {

}
