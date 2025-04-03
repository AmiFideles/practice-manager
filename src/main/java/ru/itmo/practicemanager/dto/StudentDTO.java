package ru.itmo.practicemanager.dto;

import lombok.Data;

@Data
public class StudentDTO {
    String surname;
    String name;
    String patronymic;
    String telegrammName;
    Long groupId;
    Long supervisorId;
    Long approvalStatusId;
    Boolean isCompanyApproved;
    Boolean isStatementDelivered;
    Boolean isStatementSigned;
    Boolean isStatementScanned;
}
