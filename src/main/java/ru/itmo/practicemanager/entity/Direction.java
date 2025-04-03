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
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;
    String transcript;
    @ManyToOne
    @JoinColumn(name = "faculty_id")
    Faculty faculty;
}
