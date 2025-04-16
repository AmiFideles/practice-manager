package ru.itmo.practicemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.practicemanager.entity.PracticeType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeApplicationDto {
    private Long id;
    private String status;
    private String checkStatus;
    private String isuNumber;
    private String studentName;
    private String groupNumber;
    private Long inn;
    private String organisationName;
    private String direction;
    private String location;
    private String supervisorName;
    private String mail;
    private String phone;
    private PracticeType practiceType;
}