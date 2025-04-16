package ru.itmo.practicemanager.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.practicemanager.dto.PracticeApplicationDto;
import ru.itmo.practicemanager.dto.PracticeApplicationRequest;
import ru.itmo.practicemanager.entity.*;
import ru.itmo.practicemanager.repository.*;
import ru.itmo.practicemanager.entity.CheckStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.itmo.practicemanager.entity.ApplyStatus.APPROVED;
import static ru.itmo.practicemanager.entity.ApplyStatus.REJECTED;
import static ru.itmo.practicemanager.entity.ApplyStatus.PENDING;
import static ru.itmo.practicemanager.entity.CheckStatus.OK;

@Service
@RequiredArgsConstructor
public class ApplyService {
    private final ApplyRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final OrganizationRepository organizationRepository;
    private final SupervisorRepository supervisorRepository;
    private final CompanyChecker companyChecker;
    private final ContactValidator contactValidator;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CheckStatus createOrUpdateApplication(PracticeApplicationRequest request) {
        // Находим студента по telegram username
        Student student = studentRepository.findByUserTelegramId(request.getTelegramId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден с таким телеграм именем"));

        // Проверяем существующую заявку
        Optional<Apply> existingApplication = applicationRepository.findByStudentIsuNumber(student.getIsuNumber());
        if (existingApplication.isPresent()) {
            Apply apply = existingApplication.get();
            if (apply.getStatus().equals(APPROVED)) {
                throw new IllegalStateException("Ваша заявка уже одобрена");
            }
            if (apply.getStatus().equals(ApplyStatus.PENDING)) {
                throw new IllegalStateException("Ваша заявка еще в состоянии обработки");
            }
            student.setApply(null);
            student = studentRepository.save(student);

            // Удаляем заявку
            applicationRepository.delete(apply);
            applicationRepository.flush();
        }

        // Создаем или находим организацию
        Supervisor supervisor;
        Organization organization;

        ApplyStatus status;
        CheckStatus checkStatus;

        if ("ИТМО".equalsIgnoreCase(request.getOrganisationName())) {
            organization = organizationRepository.findByName("ИТМО")
                    .orElseThrow(() -> new EntityNotFoundException("Организация ИТМО не найдена"));

            checkStatus = OK;
            status = APPROVED;

            if (request.getSupervisorName() == null || request.getSupervisorName().isBlank()) {
                Optional<Supervisor> byMail = supervisorRepository.findByMail("Маркина Т.А.");
                supervisor = byMail.orElseGet(() -> supervisorRepository.save(
                        Supervisor.builder()
                                .name("Маркина Т.А.")
                                .mail(null)
                                .phone(null)
                                .organization(organization)
                                .build()));
            } else {
                if (!contactValidator.isValidEmail(request.getMail())) {
                    checkStatus = CheckStatus.INVALID_EMAIL;
                    status = REJECTED;
                }
                else if (!contactValidator.isValidPhoneNumber(request.getPhone())) {
                    checkStatus = CheckStatus.INVALID_PHONE;
                    status = REJECTED;
                }

                Optional<Supervisor> byMail = supervisorRepository.findByMail(request.getSupervisorName());
                supervisor = byMail.orElseGet(() -> supervisorRepository.save(
                        Supervisor.builder()
                                .name(request.getSupervisorName())
                                .mail(request.getMail())
                                .phone(request.getPhone())
                                .organization(organization)
                                .build()));
            }

        } else {
            // Для внешних организаций
            if (request.getInn() == null) {
                throw new IllegalArgumentException("Для внешней организации необходимо указать ИНН");
            }

            organization = organizationRepository.findByInn(request.getInn())
                    .orElseGet(() -> organizationRepository.save(
                            Organization.builder()
                                    .inn(request.getInn())
                                    .name(request.getOrganisationName())
                                    .location(request.getLocation())
                                    .build()));

            if (request.getSupervisorName() == null || request.getSupervisorName().isBlank() || request.getMail() == null) {
                throw new IllegalArgumentException("Необходимо ввести данные руководителя практики в компании");
            }

            Optional<Supervisor> byMail = supervisorRepository.findByMail(request.getMail());
            supervisor = byMail.orElseGet(() -> supervisorRepository.save(
                    Supervisor.builder()
                            .name(request.getSupervisorName())
                            .mail(request.getMail())
                            .phone(request.getPhone())
                            .organization(organization)
                            .build()));

            if (!contactValidator.isValidEmail(request.getMail())) {
                checkStatus = CheckStatus.INVALID_EMAIL;
            }
            else if (!contactValidator.isValidPhoneNumber(request.getPhone())) {
                checkStatus = CheckStatus.INVALID_PHONE;
            }
            else {
                checkStatus = companyChecker.checkCompany(
                        organization.getInn().toString(), request.getPracticeType(), request.getOrganisationName());
            }

            status = REJECTED;
            if (checkStatus.equals(CheckStatus.API_ERROR) || checkStatus.equals(CheckStatus.JSON_PARSING_ERROR) ) {
                status = PENDING;
            }
            else if (checkStatus.equals(OK)) {
                status = APPROVED;
            }
        }

        // Создаем новую заявку
        Apply application = Apply.builder()
                .student(student)
                .organization(organization)
                .supervisor(supervisor)
                .status(status)
                .checkStatus(checkStatus)
                .practiceType(request.getPracticeType())
                .build();
        student.setApply(application);
        studentRepository.save(student);
        Apply saved = applicationRepository.save(application);

        return checkStatus;
    }

    @Transactional(readOnly = true)
    public List<PracticeApplicationDto> getApplicationsByGroupAndStatus(
            String groupName,
            ApplyStatus status) {

        List<Apply> applications = applicationRepository.findByGroupAndStatus(groupName, status);
        return applications.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PracticeApplicationDto getApplicationByIsuNumber(String isuNumber) {
        return applicationRepository.findByStudentIsuNumber(isuNumber)
                .map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));
    }

    @Transactional
    public void updateApplicationStatus(String isuNumber, ApplyStatus status) {
        Apply application = applicationRepository.findByStudentIsuNumber(isuNumber)
                .orElseThrow(() -> new EntityNotFoundException("Заявка у данного студента не существует"));

        application.setStatus(status);
        applicationRepository.save(application);
    }

    private PracticeApplicationDto mapToDto(Apply application) {
        return PracticeApplicationDto.builder()
                .id(application.getId())
                .status(application.getStatus().name())
                .status(application.getCheckStatus().name())
                .isuNumber(application.getStudent().getIsuNumber())
                .studentName(application.getStudent().getFullName())
                .groupNumber(application.getStudent().getStudyGroup().getNumber())
                .inn(application.getOrganization().getInn())
                .organisationName(application.getOrganization().getName())
                .location(application.getOrganization().getLocation())
                .supervisorName(application.getSupervisor().getName())
                .mail(application.getSupervisor() != null ? application.getSupervisor().getMail() : null)
                .phone(application.getSupervisor() != null ? application.getSupervisor().getPhone() : null)
                .practiceType(application.getPracticeType())
                .build();
    }

}