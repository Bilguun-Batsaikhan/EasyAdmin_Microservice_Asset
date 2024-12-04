package com.example.table.enumeration;

import lombok.Getter;

@Getter
public enum AssetFieldNameUpdateEnum {
    NAME("modelName"),
    TYPE("type"),
    STATUS("status"),
    PRICE("cost"),
    USER_ID("userID");

    private final String fieldName;

    AssetFieldNameUpdateEnum(String fieldName) {
        this.fieldName = fieldName;
    }
}