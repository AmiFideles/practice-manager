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
public class Direction {
    @Id
    @SequenceGenerator(
            name = "direction_seq_gen",
            sequenceName = "direction_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "direction_seq_gen"
    )
    Long id;
    String transcript;
    String number;
    String facultyName;
}
