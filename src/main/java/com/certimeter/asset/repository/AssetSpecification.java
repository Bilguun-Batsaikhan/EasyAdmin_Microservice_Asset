package com.certimeter.asset.repository;

import com.certimeter.asset.model.Asset;
import com.certimeter.asset.model.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import com.certimeter.asset.enumeration.MatchMode;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AssetSpecification {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static Specification<Asset> matchMode(String field, String value, MatchMode matchMode) {
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
            case GREATER_THAN:
                return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get(field), value);
            case LESS_THAN:
                return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get(field), value);
            case GREATER_THAN_OR_EQUALS:
                return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get(field), value);
            case LESS_THAN_OR_EQUALS:
                return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get(field), value);
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

    public static Specification<Asset> matchUsername(String username, MatchMode matchMode) {
        return (root, query, criteriaBuilder) -> {
            Join<Asset, User> userJoin = root.join("user");
            switch (matchMode) {
                case STARTS_WITH:
                    return criteriaBuilder.like(userJoin.get("username"), username + "%");
                case CONTAINS:
                    return criteriaBuilder.like(userJoin.get("username"), "%" + username + "%");
                case NOT_CONTAINS:
                    return criteriaBuilder.notLike(userJoin.get("username"), "%" + username + "%");
                case ENDS_WITH:
                    return criteriaBuilder.like(userJoin.get("username"), "%" + username);
                case EQUALS:
                    return criteriaBuilder.equal(userJoin.get("username"), username);
                case NOT_EQUALS:
                    return criteriaBuilder.notEqual(userJoin.get("username"), username);
                default:
                    return null;
            }
        };
    }
}