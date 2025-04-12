package ru.itmo.practicemanager.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long telegramId;
    private String telegramUsername;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean approved;

    @OneToOne(mappedBy = "user")
    private Student student;
}
