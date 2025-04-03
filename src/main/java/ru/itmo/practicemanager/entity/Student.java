package ru.itmo.practicemanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;
    String surname;
    String name;
    String patronymic;
    String telegrammName;
    @ManyToOne
    @JoinColumn(name = "group_id")
    StudyGroup studyGroup;
    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    Supervisor supervisor;
    @ManyToOne
    @JoinColumn(name = "approvalStatus_id")
    ApprovalStatus approvalStatus;
    Boolean isCompanyApproved;
    Boolean isStatementDelivered;
    Boolean isStatementSigned;
    Boolean isStatementScanned;
}
