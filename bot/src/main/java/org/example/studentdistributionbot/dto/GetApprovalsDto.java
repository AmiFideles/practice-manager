package org.example.studentdistributionbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetApprovalsDto {
    private String studyGroupName;
    private String status;
}
