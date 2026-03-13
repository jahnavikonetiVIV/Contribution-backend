package com.jahnavi.contribution.contribution_management.repository;

import com.jahnavi.contribution.contribution_management.entity.FundRequestContributionMapping;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for FundRequestContributionMapping filtering
 */
public class FundRequestMappingSpecification {

    private static final String TRANSACTION_DATE_TIME = "transactionDateTime";

    private FundRequestMappingSpecification() {
    }

    public static Specification<FundRequestContributionMapping> filterBy(
            String utr,
            String masterVa,
            String vaAccount,
            String folio,
            Long fundId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            addStringEqualPredicate(predicates, root, criteriaBuilder, "utr", utr);
            addStringEqualPredicate(predicates, root, criteriaBuilder, "masterVa", masterVa);
            addStringEqualPredicate(predicates, root, criteriaBuilder, "vaAccount", vaAccount);
            addStringEqualPredicate(predicates, root, criteriaBuilder, "folio", folio);
            addLongEqualPredicate(predicates, root, criteriaBuilder, "fundId", fundId);
            addDateRangePredicates(predicates, root, criteriaBuilder, dateFrom, dateTo);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addStringEqualPredicate(
            List<Predicate> predicates,
            Root<FundRequestContributionMapping> root,
            CriteriaBuilder criteriaBuilder,
            String fieldName,
            String value) {
        if (value != null && !value.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get(fieldName), value));
        }
    }

    private static void addLongEqualPredicate(
            List<Predicate> predicates,
            Root<FundRequestContributionMapping> root,
            CriteriaBuilder criteriaBuilder,
            String fieldName,
            Long value) {
        if (value != null) {
            predicates.add(criteriaBuilder.equal(root.get(fieldName), value));
        }
    }

    private static void addDateRangePredicates(
            List<Predicate> predicates,
            Root<FundRequestContributionMapping> root,
            CriteriaBuilder criteriaBuilder,
            LocalDateTime dateFrom,
            LocalDateTime dateTo) {
        if (dateFrom != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(TRANSACTION_DATE_TIME), dateFrom));
        }
        if (dateTo != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(TRANSACTION_DATE_TIME), dateTo));
        }
    }
}
