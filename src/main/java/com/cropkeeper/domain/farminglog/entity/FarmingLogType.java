package com.cropkeeper.domain.farminglog.entity;

public enum FarmingLogType {
    CULTIVATION("재배"),
    FERTILIZING("시비"),
    PEST_CONTROL("방제"),
    IRRIGATION("관수");

    private final String description;

    FarmingLogType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
