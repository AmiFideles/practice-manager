package ru.itmo.practicemanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.practicemanager.dto.OrganizationDTO;
import ru.itmo.practicemanager.entity.Organization;
import ru.itmo.practicemanager.repository.OrganizationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    public Organization getOrganizationById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Организация с id " + id + " не найдена"));
    }

    public Organization createOrganization(OrganizationDTO organizationDTO) {
        Organization organization = Organization.builder()
                .inn(organizationDTO.getInn())
                .name(organizationDTO.getName())
                .direction(organizationDTO.getDirection())
                .location(organizationDTO.getLocation())
                .build();
        return organizationRepository.save(organization);
    }

    public Organization updateOrganization(Long id, OrganizationDTO updatedOrganizationDTO) {
        Organization existingOrganization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Организация с id " + id + " не найдена"));

        existingOrganization.setInn(updatedOrganizationDTO.getInn());
        existingOrganization.setName(updatedOrganizationDTO.getName());
        existingOrganization.setDirection(updatedOrganizationDTO.getDirection());
        existingOrganization.setLocation(updatedOrganizationDTO.getLocation());

        return organizationRepository.save(existingOrganization);
    }

    public void deleteOrganization(Long id) {
        organizationRepository.deleteById(id);
    }
}
