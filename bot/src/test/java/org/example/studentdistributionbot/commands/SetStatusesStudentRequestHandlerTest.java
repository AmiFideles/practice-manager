package org.example.studentdistributionbot.commands;

import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.client.apply_controller.SetStatusesStudentRequestClient;
import org.example.studentdistributionbot.commands.apply_controller.SetStatusesStudentRequestHandler;
import org.example.studentdistributionbot.dto.ApplyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SetStatusesStudentRequestHandlerTest {

    @Mock
    private SetStatusesStudentRequestClient setStatusesStudentRequestClient;

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private Update update;

    @Mock
    private Message message;

    private SetStatusesStudentRequestHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SetStatusesStudentRequestHandler(setStatusesStudentRequestClient);
    }

    @Test
    void shouldReturnCorrectCommand() {
        assertThat(handler.getCommand()).isEqualTo(Command.SET_REQUEST_STATUS);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/set_request_status",
            "/set_request_status 123456",
            "/set_request_status 123456 PENDING EXTRA",
            "/set_request_status 123456"
    })
    void shouldHandleInvalidCommandFormat(String command) throws TelegramApiException {
        // given
        Long chatId = 123L;
        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getText()).thenReturn(command);

        // when
        handler.handleCommand(update, telegramClient);

        // then
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().contains("Введите команду правильно") &&
                        msg.getChatId().equals(chatId.toString())));
        verify(setStatusesStudentRequestClient, never()).setStatus(any(), any());
    }

    @Test
    void shouldHandleValidPendingStatus() throws TelegramApiException {
        // given
        Long chatId = 123L;
        String isuNumber = "123456";
        String command = "/set_request_status " + isuNumber + " PENDING";
        String responseMessage = "Status updated successfully";

        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getText()).thenReturn(command);
        when(setStatusesStudentRequestClient.setStatus(ApplyStatus.PENDING, isuNumber))
                .thenReturn(responseMessage);

        // when
        handler.handleCommand(update, telegramClient);

        // then
        verify(setStatusesStudentRequestClient).setStatus(ApplyStatus.PENDING, isuNumber);
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().equals(responseMessage) &&
                        msg.getChatId().equals(chatId.toString())));
    }

    @Test
    void shouldHandleValidApprovedStatus() throws TelegramApiException {
        // given
        Long chatId = 123L;
        String isuNumber = "123456";
        String command = "/set_request_status " + isuNumber + " APPROVED";
        String responseMessage = "Status updated successfully";

        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getText()).thenReturn(command);
        when(setStatusesStudentRequestClient.setStatus(ApplyStatus.APPROVED, isuNumber))
                .thenReturn(responseMessage);

        // when
        handler.handleCommand(update, telegramClient);

        // then
        verify(setStatusesStudentRequestClient).setStatus(ApplyStatus.APPROVED, isuNumber);
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().equals(responseMessage) &&
                        msg.getChatId().equals(chatId.toString())));
    }

    @Test
    void shouldHandleValidRejectedStatus() throws TelegramApiException {
        // given
        Long chatId = 123L;
        String isuNumber = "123456";
        String command = "/set_request_status " + isuNumber + " REJECTED";
        String responseMessage = "Status updated successfully";

        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getText()).thenReturn(command);
        when(setStatusesStudentRequestClient.setStatus(ApplyStatus.REJECTED, isuNumber))
                .thenReturn(responseMessage);

        // when
        handler.handleCommand(update, telegramClient);

        // then
        verify(setStatusesStudentRequestClient).setStatus(ApplyStatus.REJECTED, isuNumber);
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().equals(responseMessage) &&
                        msg.getChatId().equals(chatId.toString())));
    }

    @Test
    void shouldHandleInvalidStatus() {
        // given
        String isuNumber = "123456";
        String command = "/set_request_status " + isuNumber + " INVALID_STATUS";

        when(update.getMessage()).thenReturn(message);
        when(message.getText()).thenReturn(command);

        // when
        assertThatThrownBy(() -> handler.handleCommand(update, telegramClient)).isInstanceOf(IllegalArgumentException.class);

        // then
        verify(setStatusesStudentRequestClient, never()).setStatus(any(), any());
    }

    @Test
    void shouldHandleClientError() throws TelegramApiException {
        // given
        Long chatId = 123L;
        String isuNumber = "123456";
        String command = "/set_request_status " + isuNumber + " PENDING";
        String errorMessage = "Error updating status";

        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getText()).thenReturn(command);
        when(setStatusesStudentRequestClient.setStatus(ApplyStatus.PENDING, isuNumber))
                .thenReturn(errorMessage);

        // when
        handler.handleCommand(update, telegramClient);

        // then
        verify(setStatusesStudentRequestClient).setStatus(ApplyStatus.PENDING, isuNumber);
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().equals(errorMessage) &&
                        msg.getChatId().equals(chatId.toString())));
    }
}