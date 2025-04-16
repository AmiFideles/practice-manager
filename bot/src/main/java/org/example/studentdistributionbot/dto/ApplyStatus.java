package org.example.studentdistributionbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplyStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED");

    private final String value;

    public static ApplyStatus fromValue(String value) {
        for (ApplyStatus applyStatus : ApplyStatus.values()) {
            if (applyStatus.getValue().equalsIgnoreCase(value)) {
                return applyStatus;
            }
        }
        throw new IllegalArgumentException("Unknown command: " + value);
    }
}
