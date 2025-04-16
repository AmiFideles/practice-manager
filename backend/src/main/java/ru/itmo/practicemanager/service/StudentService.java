package ru.itmo.practicemanager.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.practicemanager.dto.ApprovalStatusDTO;
import ru.itmo.practicemanager.dto.GroupedApprovalsDto;
import ru.itmo.practicemanager.dto.StudentApprovalDto;
import ru.itmo.practicemanager.dto.UserDto;
import ru.itmo.practicemanager.entity.*;
import ru.itmo.practicemanager.exception.RegistrationException;
import ru.itmo.practicemanager.repository.StudentRepository;
import ru.itmo.practicemanager.repository.StudyGroupRepository;
import ru.itmo.practicemanager.repository.UserRepository;
import ru.itmo.practicemanager.service.excel.ParserService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {
    private final ParserService parserService;

    private final StudentRepository studentRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;


    @Transactional
    public void processStudentExcel(MultipartFile file) {
        List<Student> students = parserService.parseStudentsFromExcel(file);
        saveStudentsWithGroups(students);
    }

    private void saveStudentsWithGroups(List<Student> newStudents) {
        Set<String> existingIsuNumbers = studentRepository.findAll().stream()
                .map(Student::getIsuNumber)
                .collect(Collectors.toSet());

        List<Student> studentsToSave = new ArrayList<>();

        for (Student newStudent : newStudents) {
            if (existingIsuNumbers.contains(newStudent.getIsuNumber())) {
                continue;
            }
            StudyGroup group = studyGroupRepository.findByNumber(newStudent.getStudyGroup().getNumber())
                    .orElseGet(() -> studyGroupRepository.save(newStudent.getStudyGroup()));

            newStudent.setStudyGroup(group);
            newStudent.setApprovalStatus(ApprovalStatus.NOT_REGISTERED);
            studentsToSave.add(newStudent);
        }

        studentRepository.saveAll(studentsToSave);
    }

    @Transactional(readOnly = true)
    public Student getByIsuNumber(String isuNumber) {
        return studentRepository.findByIsuNumber(isuNumber)
                .orElseThrow(() -> new IllegalArgumentException("Студент с isu " + isuNumber + " не найден"));
    }

    @Transactional(readOnly = true)
    public List<Student> getStudentsByFilters(
            String groupNumber,
            Boolean isStatementDelivered,
            Boolean isStatementSigned,
            Boolean isStatementScanned,
            Boolean isNotificationSent) {

        return studentRepository.findByFilters(
                groupNumber,
                isStatementDelivered,
                isStatementSigned,
                isStatementScanned,
                isNotificationSent
        );
    }

    public void setStatuses(
            String studentIsuNumber,
            Boolean isStatementDelivered,
            Boolean isStatementSigned,
            Boolean isStatementScanned,
            Boolean isNotificationSent,
            String comment,
            IndividualAssignmentStatus individualAssignmentStatus
    ) {
        Student student = studentRepository.findByIsuNumber(studentIsuNumber)
                .orElseThrow(() -> new IllegalArgumentException("Студент с isu " + studentIsuNumber + " не найден"));

        if (isStatementDelivered != null) {
            student.setIsStatementDelivered(isStatementDelivered);
        }

        if (isStatementSigned != null) {
            student.setIsStatementSigned(isStatementSigned);
        }

        if (isStatementScanned != null) {
            student.setIsStatementScanned(isStatementScanned);
        }

        if (isNotificationSent != null) {
            student.setIsNotificationSent(isNotificationSent);
        }

        if (comment != null) {
            student.setComment(comment);
        }

        if (individualAssignmentStatus != null) {
            student.setIndividualAssignmentStatus(individualAssignmentStatus);
        }

        studentRepository.save(student);
    }


    // TODO обработка ошибок
    @Transactional
    public void register(UserDto request) {
        Student student = studentRepository.findByFullNameAndIsuNumber(
                request.getFullName(),
                request.getIsuNumber()
        ).orElseThrow(() -> new RegistrationException(
                "Проверьте ФИО, номер ИСУ"));

        if (student.getUser() != null) {
            throw new RegistrationException("Этот пользователь уже зарегистрирован");
        }

        User user = User.builder()
                .telegramId(request.getTelegramId())
                .telegramUsername(request.getTelegramUsername())
                .role(Role.STUDENT)
                .build();

        student.setApprovalStatus(ApprovalStatus.WAITING_FOR_APPROVAL);
        student.setUser(user);
        userRepository.save(user);
        studentRepository.save(student);
    }

    @Transactional
    public List<GroupedApprovalsDto> getGroupedApprovals(String studyGroupName, ApprovalStatus status) {
//        ApprovalStatus status = ApprovalStatus.valueOf(approvalStatusDTO.getName());
        List<Student> students = studentRepository.findByApprovalStatus(status);

        Stream<Student> studentStream = students.stream();
        if (studyGroupName != null) {
            studentStream = studentStream.filter(student ->
                    student.getStudyGroup() != null && studyGroupName.equals(student.getStudyGroup().getNumber()));
        }

        Map<String, List<Student>> studentsByGroup = studentStream
                .filter(student -> student.getStudyGroup() != null)
                .collect(Collectors.groupingBy(student -> student.getStudyGroup().getNumber()));

        return studentsByGroup.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> createGroupedApprovalsDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private GroupedApprovalsDto createGroupedApprovalsDto(String groupNumber, List<Student> students) {
        GroupedApprovalsDto dto = new GroupedApprovalsDto();
        dto.setGroupNumber(groupNumber);
        dto.setStudents(students.stream()
                .map(this::convertToStudentApprovalDto)
                .collect(Collectors.toList()));
        return dto;
    }

    private StudentApprovalDto convertToStudentApprovalDto(Student student) {
        StudentApprovalDto dto = new StudentApprovalDto();
        dto.setId(student.getId());
        dto.setIsuNumber(student.getIsuNumber());
        dto.setFullName(student.getFullName());
        if (student.getUser() != null) {
            dto.setTelegramUsername(student.getUser().getTelegramUsername());
        }
        return dto;
    }

    @Transactional
    public void approveByIsuNumber(String isuNumber, ApprovalStatusDTO approvalStatusDTO) {
        Student student = studentRepository.findByIsuNumber(isuNumber)
                .orElseThrow(() -> new EntityNotFoundException("Студент c таким ИСУ номером не найден"));
        student.setApprovalStatus(ApprovalStatus.valueOf(approvalStatusDTO.getStatus()));
        userRepository.save(student.getUser());
    }

    @Transactional
    public ApprovalStatus getApprovalStatus(Long telegramId, String isuNumber) {
        if (telegramId != null) {
            Optional<User> byTelegramId = userRepository.findByTelegramId(telegramId);
            if (byTelegramId.isPresent()) {
                User user = byTelegramId.get();
                return user.getStudent().getApprovalStatus();
            } else {
                throw new EntityNotFoundException("Студент с таким именем телеграм пользователя не найден");
            }
        } else {
            Optional<Student> byIsuNumber = studentRepository.findByIsuNumber(isuNumber);
            if (byIsuNumber.isPresent()) {
                return byIsuNumber.get().getApprovalStatus();
            } else {
                throw new EntityNotFoundException("Студент с таким ИСУ номером не найден");
            }
        }
    }

    public byte[] generateApprovalExcel() throws IOException {
        List<Student> unapprovedStudents = studentRepository.findByApprovalStatus(ApprovalStatus.WAITING_FOR_APPROVAL);

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
        boolean isApproved;

        if (checkboxCell.getCellType() == CellType.STRING) {
            isApproved = checkboxCell.getStringCellValue().equalsIgnoreCase("ДА");
        } else if (checkboxCell.getCellType() == CellType.BOOLEAN) {
            isApproved = checkboxCell.getBooleanCellValue();
        } else {
            isApproved = false;
        }

        String isuNumber = row.getCell(0).getStringCellValue();
        studentRepository.findByIsuNumber(isuNumber).ifPresent(student -> {
            if (isApproved) {
                student.setApprovalStatus(ApprovalStatus.REGISTERED);
                log.info("Student approved: ISU {}", isuNumber);
            } else {
                student.setApprovalStatus(ApprovalStatus.REJECTED);
                log.info("Student rejected: ISU {}", isuNumber);
            }
            studentRepository.save(student);
        });
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