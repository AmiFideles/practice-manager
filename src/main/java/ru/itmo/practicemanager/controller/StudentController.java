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

    @GetMapping("/report")
    public ResponseEntity<byte[]> generateStudentReport() throws IOException {
        byte[] report = reportGenerator.generateReport();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students_report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(report);
    }

    @GetMapping("/{isuNumber}")
    public ResponseEntity<Student> getStudentByIsuNumber(@PathVariable String isuNumber) {
        Student student = studentService.getByIsuNumber(isuNumber);
        return ResponseEntity.ok(student);
    }

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
