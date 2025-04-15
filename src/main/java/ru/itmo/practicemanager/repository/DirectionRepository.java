package ru.itmo.practicemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.practicemanager.entity.Direction;

import java.util.Optional;

@Repository
public interface DirectionRepository extends JpaRepository<Direction, Long> {
    Optional<Direction> findByNumber(String number);
}
