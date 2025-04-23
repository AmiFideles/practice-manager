package org.example.studentdistributionbot.commands.approvalStatusController;

import org.example.studentdistributionbot.BotState;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.UserContextStorage;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.example.studentdistributionbot.client.approvalStatusController.GetApprovalsClient;
import org.example.studentdistributionbot.dto.GetApprovalsDto;
import org.example.studentdistributionbot.dto.GroupResponseDto;
import org.example.studentdistributionbot.dto.StudentDto;
import org.example.studentdistributionbot.dto.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetApprovalsCommandHandlerTest {

    @Mock
    private UserContextStorage userContextStorage;

    @Mock
    private UserRoleResolverClient userRoleResolverClient;

    @Mock
    private GetApprovalsClient getApprovalsClient;

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private User user;

    private GetApprovalsCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetApprovalsCommandHandler(userContextStorage, userRoleResolverClient, getApprovalsClient);
    }

    @Test
    void shouldReturnCorrectCommand() {
        assertThat(handler.getCommand()).isEqualTo(Command.GET_APPROVALS);
    }

    @Test
    void shouldHandleCommandForAdmin() throws TelegramApiException {
        // given
        Long chatId = 123L;
        Long userId = 456L;
        UserRole adminRole = new UserRole("ADMIN");

        when(update.getMessage()).thenReturn(message);
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(chatId);
        when(user.getId()).thenReturn(userId);
        when(userRoleResolverClient.getUserRole(userId)).thenReturn(adminRole);

        // when
        handler.handleCommand(update, telegramClient);

        // then
        verify(userContextStorage).setState(chatId, BotState.WAITING_FOR_GROUP_NAME_AND_STATUS);
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().contains("Введите один из статусов") &&
                        msg.getChatId().equals(chatId.toString())));
    }

    @Test
    void shouldNotHandleCommandForNonAdmin() throws TelegramApiException {
        // given
        Long userId = 456L;
        UserRole userRole = new UserRole("USER");

        when(update.getMessage()).thenReturn(message);
        when(message.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(userRoleResolverClient.getUserRole(userId)).thenReturn(userRole);

        // when
        handler.handleCommand(update, telegramClient);

        // then
        verify(userContextStorage, never()).setState(any(), any());
        verify(telegramClient, never()).execute(any(SendMessage.class));
    }

    @Test
    void shouldGetApprovalsSuccessfully() throws TelegramApiException {
        // given
        Long chatId = 123L;
        GetApprovalsDto dto = new GetApprovalsDto("REGISTERED", "M3205");
        List<GroupResponseDto> response = List.of(
                GroupResponseDto.builder()
                        .groupNumber("M3205")
                        .students(List.of(
                                StudentDto.builder()
                                        .fullName("John Doe")
                                        .isuNumber("123456")
                                        .build()
                        ))
                        .build()
        );

        when(getApprovalsClient.getApprovals(dto)).thenReturn(response);

        // when
        handler.getApprovals(dto, telegramClient, chatId);

        // then
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().contains("Группа: M3205") &&
                        msg.getText().contains("John Doe") &&
                        msg.getText().contains("ISU: 123456") &&
                        msg.getChatId().equals(chatId.toString())));
    }

    @Test
    void shouldHandleEmptyApprovalsList() throws TelegramApiException {
        // given
        Long chatId = 123L;
        GetApprovalsDto dto = new GetApprovalsDto("REGISTERED", "M3205");
        List<GroupResponseDto> response = List.of();

        when(getApprovalsClient.getApprovals(dto)).thenReturn(response);

        // when
        handler.getApprovals(dto, telegramClient, chatId);

        // then
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().isEmpty() &&
                        msg.getChatId().equals(chatId.toString())));
    }
}