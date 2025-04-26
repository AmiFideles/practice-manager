package ru.itmo.practicemanager.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.itmo.practicemanager.IntegrationEnvironment;
import ru.itmo.practicemanager.dto.UserDto;
import ru.itmo.practicemanager.entity.ApprovalStatus;
import ru.itmo.practicemanager.repository.StudentRepository;
import ru.itmo.practicemanager.repository.StudyGroupRepository;
import ru.itmo.practicemanager.service.StudentService;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class ApprovalStatusControllerTest extends IntegrationEnvironment {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Test
    void downloadApprovalTemplate_shouldReturnExcelFile() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/approvals/excel"))
                .andExpect(status().isOk())
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        String contentDisposition = result.getResponse().getHeader("Content-Disposition");

        assertThat(content).isNotEmpty();
        assertThat(contentDisposition).contains("student_approvals.xlsx");
    }

    @Test
    void uploadApprovalExcel_shouldUpdateStudentApprovalStatus() throws Exception {
        // 1. Загрузка студентов
        ClassPathResource uploadStudentsResource = new ClassPathResource("isu_group_list.xls");
        InputStream uploadStudentsInputStream = uploadStudentsResource.getInputStream();

        MockMultipartFile uploadStudentsFile = new MockMultipartFile(
                "file",
                "isu_group_list.xls",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                uploadStudentsInputStream
        );
        mockMvc.perform(multipart("/api/students/upload")
                        .file(uploadStudentsFile))
                .andExpect(status().isOk());

        studentService.register(new UserDto("Искандаров Шахзодбек Хусаинович", "251645", 12345L, "tgUsername"));


        ClassPathResource approvalFile = new ClassPathResource("student_approvals.xlsx");
        InputStream approvalInputStream = approvalFile.getInputStream();

        MockMultipartFile approvalExcel = new MockMultipartFile(
                "file",
                "student_approvals.xlsx",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                approvalInputStream
        );

        mockMvc.perform(multipart("/api/approvals")
                        .file(approvalExcel))
                .andExpect(status().isOk());


        var student = studentRepository.findByIsuNumber("251645").orElseThrow();
        assertEquals(ApprovalStatus.REGISTERED, student.getApprovalStatus());
    }
}