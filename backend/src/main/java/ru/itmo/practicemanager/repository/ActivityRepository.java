package ru.itmo.practicemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.practicemanager.entity.ActivityCode;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityCode, Long> {}