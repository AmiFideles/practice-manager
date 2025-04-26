package ru.itmo.practicemanager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.practicemanager.dto.*;
import ru.itmo.practicemanager.entity.*;
import ru.itmo.practicemanager.exception.RegistrationException;
import ru.itmo.practicemanager.repository.StudentRepository;
import ru.itmo.practicemanager.repository.StudyGroupRepository;
import ru.itmo.practicemanager.repository.UserRepository;
import ru.itmo.practicemanager.service.excel.ParserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentServiceTest {

    @Mock
    private ParserService parserService;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessStudentExcel() {
        MultipartFile file = mock(MultipartFile.class);
        Student student = new Student();
        student.setIsuNumber("123");
        StudyGroup group = new StudyGroup();
        group.setNumber("P3212");
        student.setStudyGroup(group);

        when(parserService.parseStudentsFromExcel(file)).thenReturn(List.of(student));
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());
        when(studyGroupRepository.findByNumber("P3212")).thenReturn(Optional.of(group));

        studentService.processStudentExcel(file);

        verify(studentRepository).saveAll(anyList());
    }

    @Test
    void testRegister_AdminExists() {
        UserDto dto = new UserDto();
        dto.setTelegramId(1L);
        dto.setTelegramUsername("admin");

        User admin = new User();
        admin.setTelegramId(1L);
        admin.setRole(Role.ADMIN);

        when(userRepository.findByTelegramIdAndRole(1L, Role.ADMIN)).thenReturn(Optional.of(admin));

        RegistrationResponseDto response = studentService.register(dto);

        verify(userRepository).save(admin);
        assertEquals("Администратор успешно зарегистрирован", response.getMessage());
    }

    @Test
    void testRegister_StudentNewUser() {
        UserDto dto = new UserDto();
        dto.setTelegramId(1L);
        dto.setTelegramUsername("student");
        dto.setFullName("Иванов Иван Иванович");
        dto.setIsuNumber("123");

        Student student = new Student();
        student.setFullName("Иванов Иван Иванович");
        student.setIsuNumber("123");

        when(userRepository.findByTelegramIdAndRole(1L, Role.ADMIN)).thenReturn(Optional.empty());
        when(studentRepository.findByFullNameAndIsuNumber("Иванов Иван Иванович", "123"))
                .thenReturn(Optional.of(student));

        RegistrationResponseDto response = studentService.register(dto);

        verify(userRepository).save(any(User.class));
        verify(studentRepository).save(student);
        assertEquals("Запрос на регистрацию отправлен и будет рассмотрен преподавателем. Вы можете отправить заявку на согласование", response.getMessage());
    }

    @Test
    void testRegister_StudentAlreadyRegistered() {
        UserDto dto = new UserDto();
        dto.setTelegramId(1L);
        dto.setTelegramUsername("student");
        dto.setFullName("Иванов Иван Иванович");
        dto.setIsuNumber("123");

        Student student = new Student();
        student.setFullName("Иванов Иван Иванович");
        student.setIsuNumber("123");
        student.setUser(new User());

        when(userRepository.findByTelegramIdAndRole(1L, Role.ADMIN)).thenReturn(Optional.empty());
        when(studentRepository.findByFullNameAndIsuNumber("Иванов Иван Иванович", "123"))
                .thenReturn(Optional.of(student));

        assertThrows(RegistrationException.class, () -> studentService.register(dto));
    }
}
