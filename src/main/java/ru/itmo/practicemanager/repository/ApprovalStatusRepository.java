package ru.itmo.practicemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.practicemanager.entity.ApprovalStatus;

@Repository
public interface ApprovalStatusRepository extends JpaRepository<ApprovalStatus, Long> {
}
