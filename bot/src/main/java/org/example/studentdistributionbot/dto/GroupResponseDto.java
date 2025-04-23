package org.example.studentdistributionbot.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GroupResponseDto {
    private String groupNumber;
    private List<StudentDto> students;
}
