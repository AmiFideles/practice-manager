package ru.itmo.practicemanager.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.practicemanager.IntegrationEnvironment;
import ru.itmo.practicemanager.entity.Student;
import ru.itmo.practicemanager.entity.StudyGroup;
import ru.itmo.practicemanager.repository.StudentRepository;
import ru.itmo.practicemanager.repository.StudyGroupRepository;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerTest extends IntegrationEnvironment {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Test
    void testUploadFile_shouldSaveStudentsFromExcel() throws Exception {
        ClassPathResource resource = new ClassPathResource("isu_group_list.xls");
        InputStream inputStream = resource.getInputStream();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "isu_group_list.xls",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                inputStream
        );

        mockMvc.perform(multipart("/api/students/upload")
                        .file(file))
                .andExpect(status().isOk());

        List<Student> savedStudents = studentRepository.findAll();
        assertThat(savedStudents).hasSize(17);
    }

    @BeforeEach
    void clearDatabase() {
        studentRepository.deleteAll();
    }

    @Test
    @Transactional
    void testGenerateStudentReport() throws Exception {
        Student student = new Student();
        student.setIsuNumber("251645");
        student.setFullName("Искандаров Шахзодбек Хусаинович");
        StudyGroup studyGroup = new StudyGroup();
        studyGroup.setNumber("P34312");
        studyGroup = studyGroupRepository.save(studyGroup);
        student.setStudyGroup(studyGroup);
        studentRepository.save(student);

        mockMvc.perform(get("/api/students/report"))
                .andExpect(status().isOk())
                .andExpect(
                        header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students_report.xlsx"))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray()).isNotEmpty());
    }

    @Test
    void testGetStudentByIsuNumber() throws Exception {
        Student student = new Student();
        student.setIsuNumber("251645");
        student.setFullName("Искандаров Шахзодбек Хусаинович");
        studentRepository.save(student);

        mockMvc.perform(get("/api/students/251645"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isuNumber").value("251645"))
                .andExpect(jsonPath("$.fullName").value("Искандаров Шахзодбек Хусаинович"));
    }
}
