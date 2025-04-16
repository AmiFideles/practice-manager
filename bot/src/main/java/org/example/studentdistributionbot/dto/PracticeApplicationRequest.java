package org.example.studentdistributionbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeApplicationRequest {
    private Long telegramId;
    private Long inn;
    private String organisationName;
    private String direction;
    private String location;
    private String supervisorName;
    private String mail;
    private String phone;
    private PracticeType practiceType;
}