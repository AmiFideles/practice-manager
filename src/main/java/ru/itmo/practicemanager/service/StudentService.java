package ru.itmo.practicemanager.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.practicemanager.dto.GroupedApprovalsDto;
import ru.itmo.practicemanager.dto.StudentApprovalDto;
import ru.itmo.practicemanager.dto.UserDto;
import ru.itmo.practicemanager.entity.*;
import ru.itmo.practicemanager.exception.RegistrationException;
import ru.itmo.practicemanager.repository.ApprovalStatusRepository;
import ru.itmo.practicemanager.repository.StudentRepository;
import ru.itmo.practicemanager.repository.StudyGroupRepository;
import ru.itmo.practicemanager.repository.UserRepository;
import ru.itmo.practicemanager.service.excel.ParserService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {
    private final ParserService parserService;

    private final StudentRepository studentRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final ApprovalStatusRepository approvalStatusRepository;

    @Transactional
    public void processStudentExcel(MultipartFile file) {
        List<Student> students = parserService.parseStudentsFromExcel(file);
        saveStudentsWithGroups(students);
    }

    public Student getByIsuNumber(String isuNumber){
        return studentRepository.findByIsuNumber(isuNumber)
                .orElseThrow(() -> new IllegalArgumentException("Студент с isu " + isuNumber + " не найден"));
    }

    public List<Student> getByGroupNumber(Long id){
        StudyGroup group = studyGroupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Группа с id " + id + " не найдена"));
        return studentRepository.findStudentByStudyGroup(group);
    }

    public List<Student> getAllByIsCompanyDetailsFilled(){
        return studentRepository.findStudentByIsCompanyDetailsFilled(true);
    }

    public void setStatuses(
            String studentIsuNumber,
            Long approvalStatusId,
            Boolean isCompanyApproved,
            Boolean isStatementDelivered,
            Boolean isStatementSigned,
            Boolean isStatementScanned
    ) {
        Student student = studentRepository.findByIsuNumber(studentIsuNumber)
                .orElseThrow(() -> new IllegalArgumentException("Студент с isu " + studentIsuNumber + " не найден"));

        if (approvalStatusId != null) {
            ApprovalStatus status = approvalStatusRepository.findById(approvalStatusId)
                .orElseThrow(() -> new IllegalArgumentException("Статус с id " + approvalStatusId + " не найден"));
            student.setApprovalStatus(status);
        }

        if (isCompanyApproved != null) {
            student.setIsCompanyApproved(isCompanyApproved);
        }

        if (isStatementDelivered != null) {
            student.setIsStatementDelivered(isStatementDelivered);
        }

        if (isStatementSigned != null) {
            student.setIsStatementSigned(isStatementSigned);
        }

        if (isStatementScanned != null) {
            student.setIsStatementScanned(isStatementScanned);
        }

        studentRepository.save(student);
    }


    private void saveStudentsWithGroups(List<Student> students) {
        students.forEach(student -> {
            StudyGroup group = studyGroupRepository.findByNumber(student.getStudyGroup().getNumber())
                    .orElseGet(() -> studyGroupRepository.save(student.getStudyGroup()));
            student.setStudyGroup(group);
        });
        studentRepository.saveAll(students);
    }

    @Transactional
    public void registerStudent(UserDto request) {
        Student student = studentRepository.findByFullNameAndIsuNumber(
                request.getFullName(),
                request.getIsuNumber()
        ).orElseThrow(() -> new RegistrationException(
                "Студент не найден. Проверьте ФИО, номер ИСУ и группу"));

        if (student.getUser() != null) {
            throw new RegistrationException("Этот студент уже зарегистрирован");
        }

        User user = User.builder()
                .telegramId(request.getTelegramId())
                .telegramUsername(request.getTelegramUsername())
                .role(Role.STUDENT)
                .approved(false)
                .build();

        userRepository.save(user);
        student.setUser(user);
        studentRepository.save(student);
    }

    public List<GroupedApprovalsDto> getPendingApprovalsGrouped() {
        return studentRepository.findByUserApprovedFalseWithUser().stream()
                .collect(Collectors.groupingBy(
                        student -> student.getStudyGroup().getNumber(),
                        TreeMap::new,
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> {
                    GroupedApprovalsDto groupDto = new GroupedApprovalsDto();
                    groupDto.setGroupNumber(entry.getKey());

                    groupDto.setStudents(entry.getValue().stream()
                            .map(student -> {
                                StudentApprovalDto studentDto = new StudentApprovalDto();
                                studentDto.setId(student.getId());
                                studentDto.setIsuNumber(student.getIsuNumber());
                                studentDto.setFullName(student.getFullName());

                                if (student.getUser() != null) {
                                    studentDto.setTelegramUsername(student.getUser().getTelegramUsername());
                                }

                                return studentDto;
                            })
                            .collect(Collectors.toList()));

                    return groupDto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveByIsuNumber(String isuNumber, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Администратор не найден"));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Только администратор может подтверждать регистрации");
        }

        Student student = studentRepository.findByIsuNumber(isuNumber)
                .orElseThrow(() -> new EntityNotFoundException("Студент не найден"));

        if (student.getUser() == null) {
            throw new RuntimeException("Студент не начал процесс регистрации");
        }

        if (student.getUser().isApproved()) {
            throw new RuntimeException("Студент уже подтвержден");
        }

        student.getUser().setApproved(true);
        userRepository.save(student.getUser());
    }

    public byte[] generateApprovalExcel() throws IOException {
        List<Student> unapprovedStudents = studentRepository.findByUserApprovedFalseWithUser();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Pending Approvals");

            CellStyle groupHeaderStyle = createGroupHeaderStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle checkboxStyle = createCheckboxStyle(workbook);

            Map<StudyGroup, List<Student>> grouped = unapprovedStudents.stream()
                    .collect(Collectors.groupingBy(Student::getStudyGroup, TreeMap::new, Collectors.toList()));

            int rowNum = 0;
            for (Map.Entry<StudyGroup, List<Student>> entry : grouped.entrySet()) {
                rowNum = addGroupHeader(sheet, rowNum, entry.getKey(), groupHeaderStyle);

                rowNum = addColumnHeaders(sheet, rowNum, headerStyle);

                rowNum = addStudentRowsWithCheckboxes(workbook, sheet, rowNum, entry.getValue(), checkboxStyle);

                rowNum++;
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            return convertWorkbookToBytes(workbook);
        }
    }

    private int addStudentRowsWithCheckboxes(XSSFWorkbook workbook, XSSFSheet sheet,
                                             int rowNum, List<Student> students, CellStyle checkboxStyle) {
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint)
                dvHelper.createExplicitListConstraint(new String[]{"ДА", "НЕТ"});

        for (Student student : students) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getIsuNumber());
            row.createCell(1).setCellValue(student.getFullName());
            row.createCell(2).setCellValue(
                    student.getUser() != null ? student.getUser().getTelegramUsername() : "N/A"
            );

            XSSFCell checkboxCell = row.createCell(3);
            checkboxCell.setCellValue("НЕТ");
            checkboxCell.setCellStyle(checkboxStyle);

            CellRangeAddressList addressList = new CellRangeAddressList(
                    rowNum - 1, rowNum - 1, 3, 3);
            XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(
                    dvConstraint, addressList);
            validation.setSuppressDropDownArrow(true);
            validation.setShowErrorBox(true);
            sheet.addValidationData(validation);
        }
        return rowNum;
    }

    @Transactional
    public void processApprovalExcel(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (shouldProcessRow(row)) {
                    processApprovalRow(row);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean shouldProcessRow(Row row) {
        return row.getRowNum() > 0 &&
                row.getCell(0) != null &&
                row.getCell(3) != null;
    }

    private void processApprovalRow(Row row) {
        Cell checkboxCell = row.getCell(3);
        boolean isApproved = false;

        if (checkboxCell.getCellType() == CellType.STRING) {
            isApproved = checkboxCell.getStringCellValue().equalsIgnoreCase("ДА");
        } else if (checkboxCell.getCellType() == CellType.BOOLEAN) {
            isApproved = checkboxCell.getBooleanCellValue();
        }

        if (isApproved) {
            String isuNumber = row.getCell(0).getStringCellValue();
            studentRepository.findByIsuNumber(isuNumber).ifPresent(student -> {
                if (student.getUser() != null && !student.getUser().isApproved()) {
                    student.getUser().setApproved(true);
                    userRepository.save(student.getUser());
                    log.info("Approved student: ISU {}", isuNumber);
                }
            });
        }
    }

    private CellStyle createGroupHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCheckboxStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private int addGroupHeader(Sheet sheet, int rowNum, StudyGroup group, CellStyle style) {
        Row row = sheet.createRow(rowNum++);
        Cell cell = row.createCell(0);
        cell.setCellValue("Группа: " + group.getNumber());
        cell.setCellStyle(style);
        return rowNum;
    }

    private int addColumnHeaders(Sheet sheet, int rowNum, CellStyle style) {
        Row row = sheet.createRow(rowNum++);
        String[] headers = {"ISU Номер", "ФИО", "Telegram", "Подтвердить (ДА/НЕТ)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
        return rowNum;
    }

    private byte[] convertWorkbookToBytes(Workbook workbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }
}