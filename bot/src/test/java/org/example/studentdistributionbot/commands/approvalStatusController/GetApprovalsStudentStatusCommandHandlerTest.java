package org.example.studentdistributionbot.commands.approvalStatusController;

import org.example.studentdistributionbot.BotState;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.UserContextStorage;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.example.studentdistributionbot.client.approvalStatusController.GetApprovalsStudentStatusClient;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetApprovalsStudentStatusCommandHandlerTest {

    @Mock
    private UserContextStorage userContextStorage;
    
    @Mock
    private UserRoleResolverClient userRoleResolverClient;
    
    @Mock
    private GetApprovalsStudentStatusClient getApprovalsStudentStatusClient;
    
    @Mock
    private TelegramClient telegramClient;
    
    @Mock
    private Update update;
    
    @Mock
    private Message message;
    
    @Mock
    private User user;

    private GetApprovalsStudentStatusCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetApprovalsStudentStatusCommandHandler(userContextStorage, userRoleResolverClient, 
                getApprovalsStudentStatusClient);
    }

    @Test
    void shouldReturnCorrectCommand() {
        assertThat(handler.getCommand()).isEqualTo(Command.GET_STUDENT_STATUS);
    }

    @Test
    void shouldHandleCommandForAdmin() throws TelegramApiException {
        // given
        Long chatId = 123L;
        Long userId = 456L;

        when(update.getMessage()).thenReturn(message);
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(chatId);
        when(user.getId()).thenReturn(userId);
        when(userRoleResolverClient.getUserRole(userId)).thenReturn(new UserRole("ADMIN"));

        // when
        handler.handleCommand(update, telegramClient);

        // then
        verify(userContextStorage).setState(chatId, BotState.WAITING_FOR_ISU_NUMBER_FOR_STUDENT_STATUS);
        verify(telegramClient).execute(argThat((SendMessage msg) ->
            msg.getText().contains("Введите номер ИСУ студента") &&
            msg.getChatId().equals(chatId.toString())));
    }

    @Test
    void shouldNotHandleCommandForNonAdmin() throws TelegramApiException {
        // given
        Long userId = 456L;

        when(update.getMessage()).thenReturn(message);
        when(message.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(userRoleResolverClient.getUserRole(userId)).thenReturn(new UserRole("USER"));

        // when
        handler.handleCommand(update, telegramClient);

        // then
        verify(userContextStorage, never()).setState(any(), any());
        verify(telegramClient, never()).execute(any(SendMessage.class));
    }

    @Test
    void shouldGetStudentStatusSuccessfully() throws TelegramApiException {
        // given
        Long chatId = 123L;
        String isuNumber = "123456";
        String statusMessage = "Student status: REGISTERED";

        when(getApprovalsStudentStatusClient.getStudentStatus(isuNumber)).thenReturn(statusMessage);

        // when
        handler.getStudentStatus(isuNumber, telegramClient, chatId);

        // then
        verify(telegramClient).execute(argThat((SendMessage msg) ->
            msg.getText().equals(statusMessage) &&
            msg.getChatId().equals(chatId.toString())));
    }
}