package ru.itmo.practicemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.practicemanager.entity.Supervisor;

import java.util.Optional;

@Repository
public interface SupervisorRepository extends JpaRepository<Supervisor, Long> {
    Optional<Supervisor> findByMail(String mail);
}
