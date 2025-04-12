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
import ru.itmo.practicemanager.dto.GroupedApprovalsDto;
import ru.itmo.practicemanager.dto.UserDto;
import ru.itmo.practicemanager.service.StudentService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final StudentService studentService;

    @PostMapping("/register")
    public ResponseEntity<String> registerStudent(
            @RequestBody UserDto request) {
        studentService.registerStudent(request);
        return ResponseEntity.ok("Запрос на регистрацию отправлен. Вы можете проверить статус с помощью ...");
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
            summary = "Получить список на подтверждение (JSON)",
            description = "Возвращает список студентов, ожидающих подтверждения, в формате JSON"
    )
    @GetMapping("/approvals")
    public ResponseEntity<List<GroupedApprovalsDto>> getPendingApprovals() {
        return ResponseEntity.ok(studentService.getPendingApprovalsGrouped());
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
            summary = "Подтвердить регистрацию по ISU номеру",
            description = "Подтверждает регистрацию студента по номеру ISU"
    )
    @PostMapping("/approvals/{isuNumber}")
    public ResponseEntity<String> approveByIsuNumber(
            @Parameter(description = "ISU номер студента", required = true)
            @PathVariable String isuNumber,

            @Parameter(description = "ID администратора", required = true)
            @RequestParam Long adminId) {

        studentService.approveByIsuNumber(isuNumber, adminId);
        return ResponseEntity.ok("Регистрация студента с ISU " + isuNumber + " подтверждена");
    }
}
