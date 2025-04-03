package ru.itmo.practicemanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.practicemanager.dto.StudentDTO;
import ru.itmo.practicemanager.entity.ApprovalStatus;
import ru.itmo.practicemanager.entity.Student;
import ru.itmo.practicemanager.entity.StudyGroup;
import ru.itmo.practicemanager.entity.Supervisor;
import ru.itmo.practicemanager.repository.ApprovalStatusRepository;
import ru.itmo.practicemanager.repository.StudentRepository;
import ru.itmo.practicemanager.repository.StudyGroupRepository;
import ru.itmo.practicemanager.repository.SupervisorRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final SupervisorRepository supervisorRepository;
    private final ApprovalStatusRepository approvalStatusRepository;

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Student createStudent(StudentDTO studentDTO) {
        StudyGroup group = studyGroupRepository.findById(studentDTO.getGroupId())
                .orElseThrow(() -> new RuntimeException("Группа с id " + studentDTO.getGroupId() + " не найдена"));

        Supervisor supervisor = supervisorRepository.findById(studentDTO.getSupervisorId())
                .orElseThrow(() -> new RuntimeException("Руководитель с id " + studentDTO.getSupervisorId() + " не найден"));

        ApprovalStatus approvalStatus = approvalStatusRepository.findById(studentDTO.getApprovalStatusId())
                .orElseThrow(() -> new RuntimeException("Статус утверждения с id " + studentDTO.getApprovalStatusId() + " не найден"));

        Student student = Student.builder()
                .surname(studentDTO.getSurname())
                .name(studentDTO.getName())
                .patronymic(studentDTO.getPatronymic())
                .telegrammName(studentDTO.getTelegrammName())
                .studyGroup(group)
                .supervisor(supervisor)
                .approvalStatus(approvalStatus)
                .isCompanyApproved(studentDTO.getIsCompanyApproved())
                .isStatementDelivered(studentDTO.getIsStatementDelivered())
                .isStatementSigned(studentDTO.getIsStatementSigned())
                .isStatementScanned(studentDTO.getIsStatementScanned())
                .build();

        return studentRepository.save(student);
    }

    public Student updateStudent(Long id, StudentDTO updatedStudentDTO) {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Студент с id " + id + " не найден"));

        StudyGroup group = studyGroupRepository.findById(updatedStudentDTO.getGroupId())
                .orElseThrow(() -> new RuntimeException("Группа с id " + updatedStudentDTO.getGroupId() + " не найдена"));

        Supervisor supervisor = supervisorRepository.findById(updatedStudentDTO.getSupervisorId())
                .orElseThrow(() -> new RuntimeException("Руководитель с id " + updatedStudentDTO.getSupervisorId() + " не найден"));

        ApprovalStatus approvalStatus = approvalStatusRepository.findById(updatedStudentDTO.getApprovalStatusId())
                .orElseThrow(() -> new RuntimeException("Статус утверждения с id " + updatedStudentDTO.getApprovalStatusId() + " не найден"));

        existingStudent.setSurname(updatedStudentDTO.getSurname());
        existingStudent.setName(updatedStudentDTO.getName());
        existingStudent.setPatronymic(updatedStudentDTO.getPatronymic());
        existingStudent.setTelegrammName(updatedStudentDTO.getTelegrammName());
        existingStudent.setStudyGroup(group);
        existingStudent.setSupervisor(supervisor);
        existingStudent.setApprovalStatus(approvalStatus);
        existingStudent.setIsCompanyApproved(updatedStudentDTO.getIsCompanyApproved());
        existingStudent.setIsStatementDelivered(updatedStudentDTO.getIsStatementDelivered());
        existingStudent.setIsStatementSigned(updatedStudentDTO.getIsStatementSigned());
        existingStudent.setIsStatementScanned(updatedStudentDTO.getIsStatementScanned());

        return studentRepository.save(existingStudent);
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}
