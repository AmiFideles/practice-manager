package ru.itmo.practicemanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.practicemanager.dto.PracticeApplicationDto;
import ru.itmo.practicemanager.dto.PracticeApplicationRequest;
import ru.itmo.practicemanager.entity.ApplyStatus;
import ru.itmo.practicemanager.service.ApplyService;
import ru.itmo.practicemanager.service.PDFApplyService;

import java.util.List;

@RestController
@RequestMapping("/api/apply")
@RequiredArgsConstructor
public class ApplyController {
    private final ApplyService applyService;
    private final PDFApplyService pdfApplyService;

    @PostMapping
    public ResponseEntity<?> createApplication(
            @RequestBody PracticeApplicationRequest request) {
        applyService.createOrUpdateApplication(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<PracticeApplicationDto>> getApplications(
            @RequestParam(required = false) ApplyStatus status,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String isuNumber) {

        if (isuNumber != null) {
            return ResponseEntity.ok(List.of(applyService.getApplicationByIsuNumber(isuNumber)));
        } else {
            return ResponseEntity.ok(applyService.getApplicationsByGroupAndStatus(groupName, status));
        }
    }

    @PutMapping("/{isuNumber}")
    public ResponseEntity<?> updateApplicationStatus(
            @PathVariable String isuNumber,
            @RequestParam ApplyStatus status) {
        applyService.updateApplicationStatus(isuNumber, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generatePracticeApplicationPdf(
            @RequestParam Long telegramId) {

        byte[] pdfContent = pdfApplyService.generatePracticeApplicationPdf(telegramId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"apply.pdf\"")
                .body(pdfContent);
    }
}