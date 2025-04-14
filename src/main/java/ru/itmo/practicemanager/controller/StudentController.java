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

    @GetMapping("/company-filled")
    public ResponseEntity<List<Student>> getStudentsByCompanyDetailsFilledStatus() {
        List<Student> students = studentService.getAllByIsCompanyDetailsFilled();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{isuNumber}")
    public ResponseEntity<Student> getStudentByIsuNumber(@PathVariable String isuNumber) {
        Student student = studentService.getByIsuNumber(isuNumber);
        return ResponseEntity.ok(student);
    }

    @GetMapping()
    public ResponseEntity<List<Student>> getStudentsByGroupNumber(@RequestParam Long groupId) {
        List<Student> students = studentService.getByGroupNumber(groupId);
        return ResponseEntity.ok(students);
    }

    @PutMapping("/{isuNumber}/status")
    public ResponseEntity<?> updateStudentStatuses(
            @PathVariable String isuNumber,
            @RequestParam Long approvalStatusId,
            @RequestParam Boolean isCompanyApproved,
            @RequestParam Boolean isStatementDelivered,
            @RequestParam Boolean isStatementSigned,
            @RequestParam Boolean isStatementScanned
    ) {
        studentService.setStatuses(isuNumber, approvalStatusId, isCompanyApproved, isStatementDelivered, isStatementSigned, isStatementScanned);
        return ResponseEntity.ok().build();
    }
}
