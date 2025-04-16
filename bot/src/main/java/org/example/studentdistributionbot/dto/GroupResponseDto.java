package org.example.studentdistributionbot.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupResponseDto {
    private String groupNumber;
    private List<StudentDto> students;
}
