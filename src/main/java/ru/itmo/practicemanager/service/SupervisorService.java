package ru.itmo.practicemanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.practicemanager.dto.SupervisorDTO;
import ru.itmo.practicemanager.entity.Supervisor;
import ru.itmo.practicemanager.entity.Organization;
import ru.itmo.practicemanager.repository.SupervisorRepository;
import ru.itmo.practicemanager.repository.OrganizationRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupervisorService {

    private final SupervisorRepository supervisorRepository;
    private final OrganizationRepository organizationRepository;

    public List<Supervisor> getAllSupervisors() {
        return supervisorRepository.findAll();
    }

    public Optional<Supervisor> getSupervisorById(Long id) {
        return supervisorRepository.findById(id);
    }

    public Supervisor createSupervisor(SupervisorDTO supervisorDTO) {
        Organization organization = organizationRepository.findById(supervisorDTO.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Организация с id " + supervisorDTO.getOrganizationId() + " не найдена"));

        Supervisor supervisor = new Supervisor();
        supervisor.setName(supervisorDTO.getName());
        supervisor.setMail(supervisorDTO.getMail());
        supervisor.setPhone(supervisorDTO.getPhone());
        supervisor.setOrganization(organization);

        return supervisorRepository.save(supervisor);
    }

    public Supervisor updateSupervisor(Long id, SupervisorDTO updatedSupervisorDTO) {
        Supervisor existingSupervisor = supervisorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Руководитель с id " + id + " не найден"));

        Organization organization = organizationRepository.findById(updatedSupervisorDTO.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Организация с id " + updatedSupervisorDTO.getOrganizationId() + " не найдена"));

        existingSupervisor.setName(updatedSupervisorDTO.getName());
        existingSupervisor.setMail(updatedSupervisorDTO.getMail());
        existingSupervisor.setPhone(updatedSupervisorDTO.getPhone());
        existingSupervisor.setOrganization(organization);

        return supervisorRepository.save(existingSupervisor);
    }

    public void deleteSupervisor(Long id) {
        supervisorRepository.deleteById(id);
    }
}
