package ru.itmo.practicemanager.dto;

import lombok.Data;

@Data
public class StudentApprovalDto {
    private Long id;
    private String isuNumber;
    private String fullName;
    private String telegramUsername;
}