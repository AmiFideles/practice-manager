package ru.itmo.practicemanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.practicemanager.dto.PracticeApplicationDto;
import ru.itmo.practicemanager.dto.PracticeApplicationRequest;
import ru.itmo.practicemanager.entity.ApplyStatus;
import ru.itmo.practicemanager.entity.CheckResult;
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
        CheckResult checkResult = applyService.createOrUpdateApplication(request);

        return switch (checkResult) {
            case OK -> ResponseEntity.ok().body("Company is valid");
            case COMPANY_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Company with INN or OGRN " + request.getInn() + " not found");
            case ACTIVITY_NOT_SUITABLE -> ResponseEntity.badRequest()
                    .body("Company does not have required activity (Разработка программного обеспечения)");
            case LOCATION_NOT_SUITABLE -> ResponseEntity.badRequest()
                    .body("Company is not located in Saint Petersburg although the practice type is offline");
            case API_ERROR -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Failed to access API, please try again later (maybe the number of requests from one IP address per day has been exceeded 1000)");
            case JSON_PARSING_ERROR -> ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Invalid response format from API");
        };
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