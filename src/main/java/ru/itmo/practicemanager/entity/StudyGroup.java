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
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
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
