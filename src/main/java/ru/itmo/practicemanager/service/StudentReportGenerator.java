package ru.itmo.practicemanager.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.itmo.practicemanager.entity.Student;
import ru.itmo.practicemanager.entity.StudyGroup;
import ru.itmo.practicemanager.repository.StudentRepository;
import ru.itmo.practicemanager.service.excel.ExcelStyleHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentReportGenerator {
    private final StudentRepository studentRepository;

    public byte[] generateReport() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            ExcelStyleHelper styleHelper = new ExcelStyleHelper(workbook);

            Map<StudyGroup, List<Student>> studentsByGroup = studentRepository.findAll()
                    .stream()
                    .collect(Collectors.groupingBy(Student::getStudyGroup));

            studentsByGroup.forEach((group, students) -> {
                XSSFSheet sheet = workbook.createSheet(group.getNumber());
                createHeaderRow(sheet);
                fillStudentRows(sheet, students, styleHelper);
            });

            return convertToBytes(workbook);
        }
    }

    private void createHeaderRow(XSSFSheet sheet) {
        String[] headers = {
                "№", "ИСУ номер", "ФИО", "Место практики",
                "Заявка подписана", "Заявка скан", "Уведомление", "Комментарий"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
    }

    private void fillStudentRows(XSSFSheet sheet, List<Student> students, ExcelStyleHelper styleHelper) {
        int rowNum = 1;
        for (Student student : students) {
            Row row = sheet.createRow(rowNum++);
            fillStudentRow(row, student, styleHelper);
        }
    }

    private void fillStudentRow(Row row, Student student, ExcelStyleHelper styleHelper) {
        String rowStyle = determineRowStyle(student);

        row.createCell(0).setCellValue(row.getRowNum());
        row.createCell(1).setCellValue(student.getIsuNumber());

        Cell nameCell = row.createCell(2);
        nameCell.setCellValue(student.getFullName());
        nameCell.setCellStyle(styleHelper.getStyle(rowStyle));

        fillPracticePlaceCell(row.createCell(3), student, styleHelper);

        fillFlagCell(row.createCell(4), student.getIsStatementSigned(), styleHelper);
        fillFlagCell(row.createCell(5), student.getIsStatementScanned(), styleHelper);
        fillFlagCell(row.createCell(6), student.getIsNotificationSent(), styleHelper);

        row.createCell(7).setCellValue("");
    }

    private String determineRowStyle(Student student) {
//        if (student.getSupervisor() == null || student.getSupervisor().getOrganization() == null) {
//            return "PURPLE";
//        } else if (student.getSupervisor().getOrganization().getName().contains("ИТМО")) {
//            return "BLUE";
//        }
        return "GREEN";
    }

    private void fillPracticePlaceCell(Cell cell, Student student, ExcelStyleHelper styleHelper) {
//        String orgName = "--";
//        if (student.getSupervisor() != null && student.getSupervisor().getOrganization() != null) {
//            orgName = student.getSupervisor().getOrganization().getName();
//        }
//
//        cell.setCellValue(orgName);
//        cell.setCellStyle(styleHelper.getStyle(
//                Boolean.TRUE.equals(student.getIsCompanyApproved()) ? "FLAG_GREEN" : "FLAG_RED"
//        ));
    }

    private void fillFlagCell(Cell cell, Boolean flag, ExcelStyleHelper styleHelper) {
        cell.setCellValue(Boolean.TRUE.equals(flag) ? "Да" : "Нет");
        cell.setCellStyle(styleHelper.getStyle(
                Boolean.TRUE.equals(flag) ? "FLAG_GREEN" : "FLAG_RED"
        ));
    }

    private byte[] convertToBytes(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return out.toByteArray();
    }
}