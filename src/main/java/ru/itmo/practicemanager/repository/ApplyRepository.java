package ru.itmo.practicemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itmo.practicemanager.entity.ApplyStatus;
import ru.itmo.practicemanager.entity.Apply;

import java.util.List;
import java.util.Optional;

public interface ApplyRepository extends JpaRepository<Apply, Long> {
    Optional<Apply> findByStudentIsuNumber(String isuNumber);
    Optional<Apply> findByStudentUserTelegramUsername(String tgUsername);
    List<Apply> findAllByStatus(ApplyStatus status);

    @Query("SELECT pa FROM Apply pa " +
            "JOIN FETCH pa.student s " +
            "JOIN FETCH s.studyGroup " +
            "WHERE (:groupName IS NULL OR s.studyGroup.number = :groupName) " +
            "AND (:status IS NULL OR pa.status = :status)")
    List<Apply> findByGroupAndStatus(
            @Param("groupName") String groupName,
            @Param("status") ApplyStatus status);
}