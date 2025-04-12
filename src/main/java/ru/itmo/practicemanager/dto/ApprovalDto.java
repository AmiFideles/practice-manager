package ru.itmo.practicemanager.dto;

import lombok.Data;

@Data
public class ApprovalDto {
    private Long id;
    private String isuNumber;
    private String fullName;
    private String telegramUsername;
    private String groupNumber;
    private boolean approved;
}