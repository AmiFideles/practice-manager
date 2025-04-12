package ru.itmo.practicemanager.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupedApprovalsDto {
    private String groupNumber;
    private List<StudentApprovalDto> students;
}
