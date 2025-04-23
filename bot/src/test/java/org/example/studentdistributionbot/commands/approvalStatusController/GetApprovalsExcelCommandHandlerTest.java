package org.example.studentdistributionbot.commands.approvalStatusController;

import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.example.studentdistributionbot.client.approvalStatusController.GetApprovalsExcelClient;
import org.example.studentdistributionbot.dto.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetApprovalsExcelCommandHandlerTest {

    @Mock
    private UserRoleResolverClient userRoleResolverClient;

    @Mock
    private GetApprovalsExcelClient getApprovalsExcelClient;

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private User user;

    private GetApprovalsExcelCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetApprovalsExcelCommandHandler(userRoleResolverClient, getApprovalsExcelClient);
    }

    @Test
    void shouldReturnCorrectCommand() {
        assertThat(handler.getCommand()).isEqualTo(Command.GET_APPROVALS_EXCEL);
    }

    @Test
    void shouldHandleCommandForAdmin() throws TelegramApiException {
        // given
        Long chatId = 123L;
        Long userId = 456L;
        byte[] excelData = "test data".getBytes();

        when(update.getMessage()).thenReturn(message);
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(chatId);
        when(user.getId()).thenReturn(userId);
        when(userRoleResolverClient.getUserRole(userId)).thenReturn(new UserRole("ADMIN"));
        when(getApprovalsExcelClient.getApprovalsExcel()).thenReturn(excelData);

        // when
        handler.handleCommand(update, telegramClient);

        // then
        verify(telegramClient).execute(any(SendDocument.class));
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
        verify(getApprovalsExcelClient, never()).getApprovalsExcel();
        verify(telegramClient, never()).execute(any(SendDocument.class));
    }

    @Test
    void shouldHandleEmptyExcelData() throws TelegramApiException {
        // given
        Long userId = 456L;

        when(update.getMessage()).thenReturn(message);
        when(message.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(userRoleResolverClient.getUserRole(userId)).thenReturn(new UserRole("ADMIN"));
        when(getApprovalsExcelClient.getApprovalsExcel()).thenReturn(new byte[0]);

        // when
        handler.handleCommand(update, telegramClient);

        // then
        verify(telegramClient, never()).execute(any(SendDocument.class));
    }
}