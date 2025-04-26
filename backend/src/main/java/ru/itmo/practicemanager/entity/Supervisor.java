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
    @SequenceGenerator(
            name = "supervisor_seq_gen",
            sequenceName = "supervisor_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "supervisor_seq_gen"
    )
    Long id;
    String name;
    String mail;
    String phone;
    @ManyToOne
    @JoinColumn(name = "organization_id")
    Organization organization;
}
