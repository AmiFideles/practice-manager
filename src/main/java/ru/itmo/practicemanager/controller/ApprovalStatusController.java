package ru.itmo.practicemanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.practicemanager.entity.ApprovalStatus;
import ru.itmo.practicemanager.entity.Student;
import ru.itmo.practicemanager.service.ApprovalStatusService;

import java.util.List;

@RestController
@RequestMapping("/api/statuses")
@RequiredArgsConstructor
@CrossOrigin
public class ApprovalStatusController {

    private final ApprovalStatusService approvalStatusService;

    @GetMapping()
    public ResponseEntity<List<ApprovalStatus>> getAllStatuses() {
        List<ApprovalStatus> approvalStatuses = approvalStatusService.getAllApprovalStatuses();
        return ResponseEntity.ok(approvalStatuses);
    }
}
