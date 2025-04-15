package ru.itmo.practicemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.practicemanager.entity.Organization;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByName(String name);

    Optional<Organization> findByInn(Long inn);
}
