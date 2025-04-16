package ru.itmo.practicemanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCode {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;
    String code;
}
