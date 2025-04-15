package ru.itmo.practicemanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.practicemanager.dto.ApprovalStatusDTO;
import ru.itmo.practicemanager.dto.GroupedApprovalsDto;
import ru.itmo.practicemanager.entity.ApprovalStatus;
import ru.itmo.practicemanager.service.StudentService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
@CrossOrigin
public class ApprovalStatusController {

    private final StudentService studentService;

    @Operation(
            summary = "возвращает список доступных статусов регистрации"
    )
    @GetMapping("approvals/status")
    public ResponseEntity<List<ApprovalStatus>> getAllStatuses() {
        ApprovalStatus[] values = ApprovalStatus.values();
        return ResponseEntity.ok(Arrays.asList(values));
    }

    @Operation(
            summary = "Получить студентов по статусу регистрации (с группировкой)",
            description = "Возвращает список студентов, сгруппированных по группам(опционально), с указанным статусом регистрации"
    )
    @GetMapping("/approvals")
    public ResponseEntity<List<GroupedApprovalsDto>> getStudentsByApprovalStatus(
            @Parameter(description = "Номер группы для фильтрации (опционально)")
            @RequestParam(required = false) String studyGroupName,
            @Parameter(description = "Статус для фильтрации")
            @RequestParam ApprovalStatus status) {
        return ResponseEntity.ok(studentService.getGroupedApprovals(studyGroupName, status));
    }

    @Operation(
            summary = "Получить статус регистрации студента",
            description = "Возвращает текущий статус регистрации студента"
    )
    @GetMapping("/approvals/student-status")
    public ResponseEntity<ApprovalStatusDTO> getStudentApprovalStatus(
            @Parameter(description = "Идентификатор студента (TG username или ISU номер)")
            @RequestParam(required = false) String tgUsername,
            @RequestParam(required = false) String isuNumber) {

        if (tgUsername == null && isuNumber == null) {
            throw new IllegalArgumentException("Должен быть указан хотя бы один идентификатор (tgUsername или isuNumber)");
        }

        return ResponseEntity.ok(
                new ApprovalStatusDTO(studentService.getApprovalStatus(tgUsername, isuNumber).name())
        );
    }

    @Operation(
            summary = "Скачать шаблон для подтверждения",
            description = "Генерирует Excel файл с неподтвержденными студентами для массового подтверждения"
    )
    @GetMapping("/approvals/excel")
    public ResponseEntity<byte[]> downloadApprovalTemplate() throws IOException {
        byte[] excelBytes = studentService.generateApprovalExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=student_approvals.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }


    @Operation(
            summary = "Загрузить подтверждения",
            description = "Загружает Excel файл с подтверждениями регистраций студентов",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Файл успешно обработан"),
                    @ApiResponse(responseCode = "400", description = "Неверный формат файла"),
                    @ApiResponse(responseCode = "500", description = "Ошибка сервера при обработке файла")
            }
    )
    @PostMapping(
            value = "/approvals",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> uploadApprovalExcel(
            @Parameter(
                    description = "Excel файл с отметками подтверждения",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(
                                    type = "string",
                                    format = "binary"
                            )
                    )
            )
            @RequestParam("file") MultipartFile file
    ) {
        studentService.processApprovalExcel(file);
        return ResponseEntity.ok("Подтверждения успешно обработаны");
    }

    @Operation(
            summary = "Изменить статус регистрации  регистрацию по ISU номеру",
            description = "Подтверждает регистрацию студента по номеру ISU"
    )
    @PutMapping("/approvals/{isuNumber}")
    public ResponseEntity<String> approveByIsuNumber(
            @Parameter(description = "ISU номер студента", required = true)
            @PathVariable String isuNumber,
            @RequestBody ApprovalStatusDTO approvalStatusDTO) {
        studentService.approveByIsuNumber(isuNumber, approvalStatusDTO);
        return ResponseEntity.ok("Регистрация студента с ISU " + isuNumber + " подтверждена");
    }
}
