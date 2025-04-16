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
                for (int i = 0; i <= 7; i++) {
                    sheet.autoSizeColumn(i);
                }
            });

            return convertToBytes(workbook);
        }
    }

    private void createHeaderRow(XSSFSheet sheet) {
        String[] headers = {
                "№", "ИСУ номер", "ФИО", "Место практики",
                "Заявка подписана", "Заявка скан", "Уведомление", "ИЗ", "Комментарий"
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

        try {
            long isuNumber = Long.parseLong(student.getIsuNumber());
            row.createCell(1).setCellValue(isuNumber);
        } catch (NumberFormatException e) {
            row.createCell(1).setCellValue(student.getIsuNumber()); // fallback — как строка
        }

        Cell nameCell = row.createCell(2);
        nameCell.setCellValue(student.getFullName());
        if (rowStyle != null) {
            nameCell.setCellStyle(styleHelper.getStyle(rowStyle));
        }

        fillPracticePlaceCell(row.createCell(3), student, styleHelper);

        boolean showFlags = !(student.getApply() == null ||
                student.getApply().getOrganization().getName().contains("ИТМО"));

        fillFlagCell(row.createCell(4), student.getIsStatementSigned(), styleHelper, showFlags);
        fillFlagCell(row.createCell(5), student.getIsStatementScanned(), styleHelper, showFlags);
        fillFlagCell(row.createCell(6), student.getIsNotificationSent(), styleHelper, showFlags);

        // Индивидуальное задание (ИЗ)
        Cell izCell = row.createCell(7);
        if (student.getIndividualAssignmentStatus() != null) {
            izCell.setCellValue(student.getIndividualAssignmentStatus().getDescription());
            switch (student.getIndividualAssignmentStatus()) {
                case APPROVED -> izCell.setCellStyle(styleHelper.getStyle("GREEN"));
                case CREATED -> izCell.setCellStyle(styleHelper.getStyle("ORANGE"));
                case PENDING_SUPERVISOR -> izCell.setCellStyle(styleHelper.getStyle("YELLOW"));
                default -> {
                } // остальные не окрашиваем
            }
        } else {
            izCell.setCellValue("");
        }

        // Комментарий
        row.createCell(8).setCellValue(student.getComment() != null ? student.getComment() : "");
    }


    private String determineRowStyle(Student student) {
        if (student.getApply() == null || (student.getApprovalStatus() != null &&
                student.getApprovalStatus().name().equals("NOT_REGISTERED"))) {
            return "PURPLE";
        } else if (student.getApply().getOrganization().getName().contains("ИТМО")) {
            return "BLUE";
        } else if (student.getApply().getStatus().name().equals("APPROVED")
                && Boolean.TRUE.equals(student.getIsStatementScanned())
                && Boolean.TRUE.equals(student.getIsStatementSigned())
                && Boolean.TRUE.equals(student.getIsNotificationSent())) {
            return "GREEN";
        } else if (student.getApprovalStatus() != null &&
                student.getApprovalStatus().name().equals("WAITING_FOR_APPROVAL")) {
            return "GRAY";
        } else if (student.getApply().getStatus().name().equals("REJECTED")
                || student.getIsStatementScanned() != Boolean.TRUE
                || student.getIsStatementSigned() != Boolean.TRUE
                || student.getIsNotificationSent() != Boolean.TRUE) {
            return "YELLOW";
        } else {
            return null; // дефолтный белый
        }
    }


    private void fillPracticePlaceCell(Cell cell, Student student, ExcelStyleHelper styleHelper) {
        String orgName = "ИТМО";
        if (student.getApply() != null && student.getApply().getOrganization() != null) {
            orgName = student.getApply().getOrganization().getName() + ", " + student.getApply().getSupervisor().getName();
        }

        cell.setCellValue(orgName);

        if (student.getApply() == null) {
            cell.setCellStyle(styleHelper.getStyle("GREEN"));
        } else if (student.getApply().getStatus().name().equals("APPROVED")) {
            cell.setCellStyle(styleHelper.getStyle("GREEN"));
        } else if (student.getApply().getStatus().name().equals("REJECTED") &&
                !orgName.contains("ИТМО")) {
            cell.setCellStyle(styleHelper.getStyle("FLAG_RED"));
        } else {
            cell.setCellStyle(null);
        }
    }

    private void fillFlagCell(Cell cell, Boolean flag, ExcelStyleHelper styleHelper, boolean showFlag) {
        if (showFlag) {
            cell.setCellValue(Boolean.TRUE.equals(flag) ? "Да" : "Нет");
            cell.setCellStyle(styleHelper.getStyle(
                    Boolean.TRUE.equals(flag) ? "FLAG_GREEN" : "FLAG_RED"
            ));
        } else {
            cell.setCellValue("—");
            cell.setCellStyle(styleHelper.getStyle("FLAG_LIGHT_GREEN"));
        }
    }

    private byte[] convertToBytes(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return out.toByteArray();
    }
}
