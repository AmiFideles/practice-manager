package ru.itmo.practicemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.practicemanager.entity.Faculty;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
}
