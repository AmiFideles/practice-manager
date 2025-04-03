package ru.itmo.practicemanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.practicemanager.dto.ApprovalStatusDTO;
import ru.itmo.practicemanager.entity.ApprovalStatus;
import ru.itmo.practicemanager.service.ApprovalStatusService;

import java.util.List;

@RestController
@RequestMapping("/api/approval-statuses")
@RequiredArgsConstructor
@CrossOrigin
public class ApprovalStatusController {

    private final ApprovalStatusService approvalStatusService;

    @GetMapping
    public ResponseEntity<List<ApprovalStatus>> getAllApprovalStatuses() {
        return ResponseEntity.ok(approvalStatusService.getAllApprovalStatuses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApprovalStatus> getApprovalStatusById(@PathVariable Long id) {
        return ResponseEntity.ok(approvalStatusService.getApprovalStatusById(id));
    }

    @PostMapping
    public ResponseEntity<ApprovalStatus> createApprovalStatus(@RequestBody ApprovalStatusDTO approvalStatusDTO) {
        return ResponseEntity.ok(approvalStatusService.createApprovalStatus(approvalStatusDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApprovalStatus> updateApprovalStatus(@PathVariable Long id,
                                                               @RequestBody ApprovalStatusDTO updatedApprovalStatusDTO) {
        return ResponseEntity.ok(approvalStatusService.updateApprovalStatus(id, updatedApprovalStatusDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApprovalStatus(@PathVariable Long id) {
        approvalStatusService.deleteApprovalStatus(id);
        return ResponseEntity.noContent().build();
    }
}
