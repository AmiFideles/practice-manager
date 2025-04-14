package ru.itmo.practicemanager.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String surname;
    private String name;
    private String patronymic;
    private String fullName;
    private String isuNumber;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private StudyGroup studyGroup;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private Supervisor supervisor;

    @ManyToOne
    @JoinColumn(name = "approval_status_id")
    private ApprovalStatus approvalStatus;

    private Boolean isCompanyApproved;
    private Boolean isStatementDelivered;
    private Boolean isStatementSigned;
    private Boolean isStatementScanned;
    private Boolean isNotificationSent;
    private Boolean isCompanyDetailsFilled;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
