package ru.itmo.practicemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.practicemanager.entity.ApprovalStatus;
import ru.itmo.practicemanager.entity.Student;
import ru.itmo.practicemanager.entity.StudyGroup;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByFullNameAndIsuNumber(String fullName, String isuNumber);

    Optional<Student> findByIsuNumber(String isuNumber);

    List<Student> findByApprovalStatus(ApprovalStatus approvalStatus);

    Optional<Student> findByUserTelegramId(Long telegramId);

    @Query("SELECT s FROM Student s " +
            "JOIN FETCH s.studyGroup sg " +
            "WHERE (:groupNumber IS NULL OR sg.number = :groupNumber) " +
            "AND (:isStatementDelivered IS NULL OR s.isStatementDelivered = :isStatementDelivered) " +
            "AND (:isStatementSigned IS NULL OR s.isStatementSigned = :isStatementSigned) " +
            "AND (:isStatementScanned IS NULL OR s.isStatementScanned = :isStatementScanned) " +
            "AND (:isNotificationSent IS NULL OR s.isNotificationSent = :isNotificationSent)")
    List<Student> findByFilters(
            @Param("groupNumber") String groupNumber,
            @Param("isStatementDelivered") Boolean isStatementDelivered,
            @Param("isStatementSigned") Boolean isStatementSigned,
            @Param("isStatementScanned") Boolean isStatementScanned,
            @Param("isNotificationSent") Boolean isNotificationSent);
}
