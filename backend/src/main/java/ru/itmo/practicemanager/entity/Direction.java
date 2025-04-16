package ru.itmo.practicemanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    String number;
    String facultyName;
}
