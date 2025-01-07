package com.certimeter.asset.repository;

import com.certimeter.asset.model.AssetHistory;
import com.certimeter.asset.enumeration.MatchMode;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AssetHistorySpecification {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static Specification<AssetHistory> matchMode(String field, String value, MatchMode matchMode) {
        switch (matchMode) {
            case STARTS_WITH:
                return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(field), value + "%");
            case CONTAINS:
                return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(field), "%" + value + "%");
            case NOT_CONTAINS:
                return (root, query, criteriaBuilder) -> criteriaBuilder.notLike(root.get(field), "%" + value + "%");
            case ENDS_WITH:
                return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(field), "%" + value);
            case EQUALS:
                return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(field), value);
            case NOT_EQUALS:
                return (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get(field), value);
            case DATE_BEFORE:
                return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get(field), LocalDate.parse(value, formatter));
            case DATE_AFTER:
                return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get(field), LocalDate.parse(value, formatter));
            case DATE_IS:
                return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(field), LocalDate.parse(value, formatter));
            case DATE_IS_NOT:
                return (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get(field), LocalDate.parse(value, formatter));
            default:
                return null;
        }
    }
}