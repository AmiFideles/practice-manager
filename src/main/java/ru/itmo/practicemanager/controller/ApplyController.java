package ru.itmo.practicemanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.practicemanager.dto.ErrorResponse;
import ru.itmo.practicemanager.dto.PracticeApplicationDto;
import ru.itmo.practicemanager.dto.PracticeApplicationRequest;
import ru.itmo.practicemanager.entity.ApplyStatus;
import ru.itmo.practicemanager.entity.CheckStatus;
import ru.itmo.practicemanager.service.ApplyService;
import ru.itmo.practicemanager.service.PDFApplyService;

import java.util.List;

@RestController
@RequestMapping("/api/apply")
@RequiredArgsConstructor
public class ApplyController {
    private final ApplyService applyService;
    private final PDFApplyService pdfApplyService;

    @Operation(summary = "Создать заявку на согласование компании.")
    @PostMapping
    public ResponseEntity<?> createApplication(
            @RequestBody PracticeApplicationRequest request) {
        CheckStatus checkStatus = applyService.createOrUpdateApplication(request);

        return switch (checkStatus) {
            case OK -> ResponseEntity.ok().build();
            case COMPANY_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Организация с ИНН или ОГРН " + request.getInn() + " не найдена"));
            case ACTIVITY_NOT_SUITABLE -> ResponseEntity.badRequest()
                    .body(new ErrorResponse("Организация не имеет IT виды деятельности среди основного или дополнительных"));
            case LOCATION_NOT_SUITABLE -> ResponseEntity.badRequest()
                    .body(new ErrorResponse("Организация не располагается в Санкт-Петербурге, хотя формат практики очный"));
            case API_ERROR -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ErrorResponse("Ошибка обращения к внешнему API (возможно, превышен лимит 1000 запросов в день)"));
            case JSON_PARSING_ERROR -> ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ErrorResponse("Невалидный ответ от внешнего API"));
            case INVALID_COMPANY_NAME -> ResponseEntity.badRequest()
                    .body(new ErrorResponse("Имя компании не соответствует официальному"));
            case INVALID_EMAIL -> ResponseEntity.badRequest()
                    .body(new ErrorResponse("Невалидный email"));
            case INVALID_PHONE -> ResponseEntity.badRequest()
                    .body(new ErrorResponse("Невалидный номер телефона"));
        };
    }

    @Operation(summary = "Получить выборку заявок на согласование по заданным фильтрам. Фильтры могут комбинироваться.")
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

    @Operation(summary = "Устанавливаем студенту статусы его заявки по его ISU номеру.")
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