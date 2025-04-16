package org.example.studentdistributionbot.dto;

import lombok.Data;

@Data
public class StudentsResponseDto {
    private String fullName;
    private String isuNumber;
    private StudyGroupDto studyGroup;
}
