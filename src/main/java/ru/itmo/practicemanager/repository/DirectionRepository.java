package ru.itmo.practicemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.practicemanager.entity.Direction;

@Repository
public interface DirectionRepository extends JpaRepository<Direction, Long> {
}
