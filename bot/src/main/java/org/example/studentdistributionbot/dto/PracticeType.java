package org.example.studentdistributionbot.dto;

import lombok.Getter;

@Getter
public enum PracticeType {
    ONLINE("online"),
    OFFLINE("offline");

    private final String value;

    PracticeType(String value) {
        this.value = value;
    }

    public static PracticeType fromValue(String value) {
        for (PracticeType practiceType : PracticeType.values()) {
            if (practiceType.getValue().equalsIgnoreCase(value)) {
                return practiceType;
            }
        }
        throw new IllegalArgumentException("Unknown command: " + value);
    }
}
