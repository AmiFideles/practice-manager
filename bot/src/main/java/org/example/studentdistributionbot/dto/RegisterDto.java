package org.example.studentdistributionbot.dto;

import lombok.Data;

@Data
public class RegisterDto {
    private String fullName;
    private String isuNumber;
    private Long telegramId;
    private String telegramUsername;
}
