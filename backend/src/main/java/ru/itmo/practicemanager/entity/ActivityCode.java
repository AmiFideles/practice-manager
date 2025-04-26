package ru.itmo.practicemanager.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ActivityCode {
    @Id
    @SequenceGenerator(
            name = "activity_code_seq_gen",
            sequenceName = "activity_code_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "activity_code_seq_gen"
    )
    private Long id;

    @Column(nullable = false)
    private String code;
}