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
public class Supervisor {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;
    String surname;
    String name;
    String patronymic;
    String mail;
    String phone;
    @ManyToOne
    @JoinColumn(name = "organization_id")
    Organization organization;
}
