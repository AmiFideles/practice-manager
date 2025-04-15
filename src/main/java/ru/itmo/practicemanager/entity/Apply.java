package ru.itmo.practicemanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Apply {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ApplyStatus status;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private Supervisor supervisor;

    @OneToOne
    @JoinColumn(name = "student_id")
    @JsonIgnore
    private Student student;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Enumerated(EnumType.STRING)
    private PracticeType practiceType;
}