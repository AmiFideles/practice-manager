package org.example.studentdistributionbot.commands;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.example.studentdistributionbot.UserContextStorage;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.example.studentdistributionbot.client.approvalStatusController.*;
import org.example.studentdistributionbot.commands.approvalStatusController.*;
import org.example.studentdistributionbot.dto.ApprovalStatusDTO;
import org.example.studentdistributionbot.dto.GetApprovalsDto;
import org.example.studentdistributionbot.dto.UserRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ApprovalStatusIntegrationTest {

    private static MockWebServer mockWebServer;
    private UserContextStorage userContextStorage;
    private UserRoleResolverClient userRoleResolverClient;
    private TelegramClient telegramClient;
    private WebClient webClient;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        userContextStorage = new UserContextStorage();
        userRoleResolverClient = mock(UserRoleResolverClient.class);
        telegramClient = mock(TelegramClient.class);
    }

    @Test
    void testGetApprovalsIntegration() throws Exception {
        String expectedResponse = """
                [
                    {
                        "groupNumber": "M3205",
                        "students": [
                            {
                                "fullName": "John Doe",
                                "isuNumber": "123456"
                            }
                        ]
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(expectedResponse));

        GetApprovalsClient getApprovalsClient = new GetApprovalsClient(webClient);
        GetApprovalsCommandHandler handler = new GetApprovalsCommandHandler(
                userContextStorage, userRoleResolverClient, getApprovalsClient);

        Update update = createUpdateWithCommand("/get_approvals");
        when(userRoleResolverClient.getUserRole(anyLong())).thenReturn(new UserRole("ADMIN"));

        handler.handleCommand(update, telegramClient);
        GetApprovalsDto dto = new GetApprovalsDto("REGISTERED", "M3205");
        handler.getApprovals(dto, telegramClient, 123L);

        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).contains("/api/approvals?studyGroupName=REGISTERED&status=M3205");
    }

    @Test
    void testPutApprovalsIntegration() throws Exception {
        String expectedResponse = "Status updated successfully";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(expectedResponse));

        PutApprovalsIsuNumberClient putApprovalsClient = new PutApprovalsIsuNumberClient(webClient);
        PutApprovalsIsuNumberHandler handler = new PutApprovalsIsuNumberHandler(
                userContextStorage, userRoleResolverClient, putApprovalsClient);

        Update update = createUpdateWithCommand("/put_approvals");
        when(userRoleResolverClient.getUserRole(anyLong())).thenReturn(new UserRole("ADMIN"));

        handler.handleCommand(update, telegramClient);
        ApprovalStatusDTO statusDTO = new ApprovalStatusDTO("REGISTERED");
        handler.putApprovals(statusDTO, "123456", telegramClient, 123L);

        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(recordedRequest.getMethod()).isEqualTo("PUT");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/approvals/123456");
        assertThat(recordedRequest.getBody().readUtf8()).contains("REGISTERED");
    }

    @Test
    void testGetStudentStatusIntegration() throws Exception {
        String expectedResponse = "Student status: REGISTERED";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(expectedResponse));

        GetApprovalsStudentStatusClient statusClient = new GetApprovalsStudentStatusClient(webClient);
        GetApprovalsStudentStatusCommandHandler handler = new GetApprovalsStudentStatusCommandHandler(
                userContextStorage, userRoleResolverClient, statusClient);

        Update update = createUpdateWithCommand("/get_student_status");
        when(userRoleResolverClient.getUserRole(anyLong())).thenReturn(new UserRole("ADMIN"));

        handler.handleCommand(update, telegramClient);
        handler.getStudentStatus("123456", telegramClient, 123L);

        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/approvals/student-status?isuNumber=123456");
    }

    @Test
    void testGetApprovalsExcelIntegration() throws Exception {
        byte[] excelData = "fake excel data".getBytes();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .setBody(new String(excelData)));

        GetApprovalsExcelClient excelClient = new GetApprovalsExcelClient(webClient);
        GetApprovalsExcelCommandHandler handler = new GetApprovalsExcelCommandHandler(
                userRoleResolverClient, excelClient);

        Update update = createUpdateWithCommand("/get_approvals_excel");
        when(userRoleResolverClient.getUserRole(anyLong())).thenReturn(new UserRole("ADMIN"));

        handler.handleCommand(update, telegramClient);

        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/approvals/excel");
    }

    @Test
    void testPostApprovalsExcelIntegration() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        PostApprovalsExcelClient realPostApprovalsExcelClient = new PostApprovalsExcelClient(webClient);

        PostApprovalsExcelCommandHandler handler = new PostApprovalsExcelCommandHandler(
                userContextStorage, userRoleResolverClient, realPostApprovalsExcelClient);

        Update update = createUpdateWithCommand("/post_approvals_excel");
        when(userRoleResolverClient.getUserRole(anyLong())).thenReturn(new UserRole("ADMIN"));

        handler.handleCommand(update, telegramClient);

        byte[] testContent = "Test Excel Content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(testContent);

        handler.loadFile("test.xlsx", inputStream, telegramClient, 123L);

        RecordedRequest recordedRequest = mockWebServer.takeRequest(10, TimeUnit.SECONDS);
        assertThat(recordedRequest).isNotNull();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/approvals");
        assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE))
                .contains(MediaType.MULTIPART_FORM_DATA_VALUE);
    }

    private Update createUpdateWithCommand(String command) {
        Update update = new Update();
        Message message = new Message();
        User user = User.builder()
                .id(123L)
                .firstName("testName")
                .isBot(true)
                .build();
        message.setChat(Chat.builder()
                .id(123L)
                .type("test")
                .build());
        message.setFrom(user);
        message.setText(command);
        update.setMessage(message);

        return update;
    }
}