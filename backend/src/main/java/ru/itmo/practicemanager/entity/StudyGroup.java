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
public class StudyGroup implements Comparable<StudyGroup> {
    @Id
    @SequenceGenerator(
            name = "study_group_seq_gen",
            sequenceName = "study_group_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "study_group_seq_gen"
    )
    Long id;
    String number;
    @ManyToOne
    @JoinColumn(name = "direction_id")
    Direction direction;

    @Override
    public int compareTo(StudyGroup other) {
        return this.number.compareTo(other.number);
    }
}
