package ru.itmo.practicemanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.practicemanager.entity.IndividualAssignmentStatus;
import ru.itmo.practicemanager.entity.Student;
import ru.itmo.practicemanager.service.StudentReportGenerator;
import ru.itmo.practicemanager.service.StudentService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin
public class StudentController {

    private final StudentService studentService;
    private final StudentReportGenerator reportGenerator;

    @Operation(summary = "Загрузить файл со студентами")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @Parameter(
                    description = "Excel-файл со списком студентов",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestParam("file") MultipartFile file
    ) {
        studentService.processStudentExcel(file);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить файл со студентами")
    @GetMapping("/report")
    public ResponseEntity<byte[]> generateStudentReport() throws IOException {
        byte[] report = reportGenerator.generateReport();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students_report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(report);
    }

    @Operation(summary = "Получить студента по его ISU номеру")
    @GetMapping("/{isuNumber}")
    public ResponseEntity<Student> getStudentByIsuNumber(@PathVariable String isuNumber) {
        Student student = studentService.getByIsuNumber(isuNumber);
        return ResponseEntity.ok(student);
    }

    @Operation(summary = "Получить выборку студентов по заданным фильтрам. Фильтры могут комбинироваться.")
    @GetMapping("")
    public ResponseEntity<List<Student>> getStudentsByFilters(
            @RequestParam(required = false) String groupNumber,
            @RequestParam(required = false) Boolean isStatementDelivered,
            @RequestParam(required = false) Boolean isStatementSigned,
            @RequestParam(required = false) Boolean isStatementScanned,
            @RequestParam(required = false) Boolean isNotificationSent) {

        List<Student> students = studentService.getStudentsByFilters(
                groupNumber,
                isStatementDelivered,
                isStatementSigned,
                isStatementScanned,
                isNotificationSent
        );

        return ResponseEntity.ok(students);
    }

    @Operation(
            summary = "Устанавливаем студенту статусы по его ISU номеру.",
            description = "Все параметры необязательные. Предполагается наличие отдельной команды в боте для каждого параметра.\n\n"
                    + "Обозначения параметров:\n\n"
                    + "• isStatementDelivered - Студент принёс заявку\n\n"
                    + "• isStatementSigned - Студент подписал заявку\n\n"
                    + "• isStatementScanned - Студент принёс скан заявки\n\n"
                    + "• isNotificationSent - У студента есть уведомление\n\n"
                    + "• comment - Поле для комментария\n\n"
                    + "• individualAssignmentStatus - Статус итогового задания"
    )
    @PutMapping("/{isuNumber}/status")
    public ResponseEntity<?> updateStudentStatuses(
            @PathVariable String isuNumber,
            @RequestParam(required = false) Boolean isStatementDelivered,
            @RequestParam(required = false) Boolean isStatementSigned,
            @RequestParam(required = false) Boolean isStatementScanned,
            @RequestParam(required = false) Boolean isNotificationSent,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) IndividualAssignmentStatus individualAssignmentStatus
    ) {
        studentService.setStatuses(isuNumber, isStatementDelivered, isStatementSigned, isStatementScanned, isNotificationSent, comment, individualAssignmentStatus);
        return ResponseEntity.ok().build();
    }
}
