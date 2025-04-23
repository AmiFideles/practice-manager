package org.example.studentdistributionbot.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentDto {
    private int id;
    private String isuNumber;
    private String fullName;
    private String telegramUsername;
}
