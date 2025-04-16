package ru.itmo.practicemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.practicemanager.entity.StudyGroup;

import java.util.Optional;


@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    Optional<StudyGroup> findByNumber(String number);
}
