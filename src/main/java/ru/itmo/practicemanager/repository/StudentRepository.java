package ru.itmo.practicemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.itmo.practicemanager.entity.Student;
import ru.itmo.practicemanager.entity.StudyGroup;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByFullNameAndIsuNumber(String fullName, String isuNumber);

    Optional<Student> findByIsuNumber(String isuNumber);

    @Query("SELECT s FROM Student s JOIN FETCH s.user u WHERE u.approved = false")
    List<Student> findByUserApprovedFalseWithUser();

    List<Student> findStudentByIsCompanyDetailsFilled(Boolean isCompanyDetailsFilled);

    List<Student> findStudentByStudyGroup(StudyGroup studyGroup);
}
