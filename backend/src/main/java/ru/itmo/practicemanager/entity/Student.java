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
    @SequenceGenerator(
            name = "student_seq_gen",
            sequenceName = "student_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "student_seq_gen"
    )
    private Long id;

    private String fullName;
    private String isuNumber;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private StudyGroup studyGroup;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    private Boolean isStatementDelivered;
    private Boolean isStatementSigned;
    private Boolean isStatementScanned;
    private Boolean isNotificationSent;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "student")
    private Apply apply;

    private String comment;

    @Enumerated(EnumType.STRING)
    private IndividualAssignmentStatus individualAssignmentStatus;
}
