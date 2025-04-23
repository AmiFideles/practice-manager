package org.example.studentdistributionbot.commands;

import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.client.apply_controller.GetApplyPdfClient;
import org.example.studentdistributionbot.commands.apply_controller.GetApplyPdfCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetApplyPdfCommandHandlerTest {

    @Mock
    private GetApplyPdfClient getApplyPdfClient;

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private User user;

    private GetApplyPdfCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetApplyPdfCommandHandler(getApplyPdfClient);
    }

    @Test
    void shouldReturnCorrectCommand() {
        assertThat(handler.getCommand()).isEqualTo(Command.GET_APPLY_PDF);
    }

    @Test
    void shouldHandleCommandSuccessfully() throws Exception {
        // given
        Long chatId = 123L;
        Long userId = 456L;
        byte[] pdfContent = "test pdf content".getBytes();

        when(update.getMessage()).thenReturn(message);
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(chatId);
        when(user.getId()).thenReturn(userId);
        when(getApplyPdfClient.getPdfForTelegramId(userId)).thenReturn(pdfContent);

        // when
        handler.handleCommand(update, telegramClient);

        // then
        verify(telegramClient).execute(argThat((SendDocument doc) ->
                doc.getChatId().equals(chatId.toString()) &&
                        doc.getCaption().equals("📄 Ваша заявка на практику") &&
                        doc.getDocument() != null));
    }

    @Test
    void shouldHandleErrorWhenGettingPdf() throws Exception {
        // given
        Long chatId = 123L;
        Long userId = 456L;

        when(update.getMessage()).thenReturn(message);
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(chatId);
        when(user.getId()).thenReturn(userId);
        when(getApplyPdfClient.getPdfForTelegramId(userId))
                .thenThrow(new RuntimeException("PDF generation error"));

        // when
        handler.handleCommand(update, telegramClient);

        // then
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().equals("При формировании PDF что-то пошло не так") &&
                        msg.getChatId().equals(chatId.toString())));
    }

    @Test
    void shouldSendPdfToUser() throws Exception {
        // given
        Long chatId = 123L;
        byte[] pdfContent = "test pdf content".getBytes();

        // when
        handler.sendPdfToUser(chatId, telegramClient, pdfContent);

        // then
        verify(telegramClient).execute(argThat((SendDocument doc) ->
                doc.getChatId().equals(chatId.toString()) &&
                        doc.getCaption().equals("📄 Ваша заявка на практику") &&
                        doc.getDocument() != null));
    }

    @Test
    void shouldHandleExceptionWhenSendingPdf() throws Exception {
        // given
        Long chatId = 123L;
        Long userId = 456L;

        when(update.getMessage()).thenReturn(message);
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(chatId);
        when(user.getId()).thenReturn(userId);
        when(getApplyPdfClient.getPdfForTelegramId(userId)).thenReturn(new byte[0]);
        when(telegramClient.execute(any(SendDocument.class)))
                .thenThrow(new TelegramApiException("Error sending document"));

        // when
        handler.handleCommand(update, telegramClient);

        // then
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().equals("При формировании PDF что-то пошло не так") &&
                        msg.getChatId().equals(chatId.toString())));
    }
}