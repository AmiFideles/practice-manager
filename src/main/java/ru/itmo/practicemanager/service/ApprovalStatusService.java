package ru.itmo.practicemanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.practicemanager.dto.ApprovalStatusDTO;
import ru.itmo.practicemanager.entity.ApprovalStatus;
import ru.itmo.practicemanager.repository.ApprovalStatusRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalStatusService {

    private final ApprovalStatusRepository approvalStatusRepository;

    public List<ApprovalStatus> getAllApprovalStatuses() {
        return approvalStatusRepository.findAll();
    }

    public ApprovalStatus getApprovalStatusById(Long id) {
        return approvalStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ApprovalStatus с id " + id + " не найден"));
    }

    public ApprovalStatus createApprovalStatus(ApprovalStatusDTO approvalStatusDTO) {
        ApprovalStatus approvalStatus = ApprovalStatus.builder()
                .name(approvalStatusDTO.getName())
                .build();
        return approvalStatusRepository.save(approvalStatus);
    }

    public ApprovalStatus updateApprovalStatus(Long id, ApprovalStatusDTO updatedApprovalStatusDTO) {
        ApprovalStatus existingApprovalStatus = approvalStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ApprovalStatus с id " + id + " не найден"));

        existingApprovalStatus.setName(updatedApprovalStatusDTO.getName());
        return approvalStatusRepository.save(existingApprovalStatus);
    }

    public void deleteApprovalStatus(Long id) {
        approvalStatusRepository.deleteById(id);
    }
}
