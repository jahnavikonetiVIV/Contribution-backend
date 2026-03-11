package com.jahnavi.contribution.contribution_management.repository;

import com.jahnavi.contribution.contribution_management.entity.EcollectRawTxn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EcollectRawTxnRepository extends JpaRepository<EcollectRawTxn, Long> {
    
    Optional<EcollectRawTxn> findByTransferUniqueNo(String transferUniqueNo);
    
    boolean existsByTransferUniqueNo(String transferUniqueNo);
}

