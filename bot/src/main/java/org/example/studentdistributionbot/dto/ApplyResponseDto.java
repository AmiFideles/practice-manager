package org.example.studentdistributionbot.dto;

import lombok.Data;

@Data
public class ApplyResponseDto {
    private Long id;
    private String status;
    private String checkStatus;
    private String isuNumber;
    private String studentName;
    private String groupNumber;
    private String inn;
    private String organisationName;
    private String location;
    private String supervisorName;
    private String mail;
    private String phone;
    private String practiceType;
}
