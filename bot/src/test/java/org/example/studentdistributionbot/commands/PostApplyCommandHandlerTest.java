package org.example.studentdistributionbot.commands;

import org.example.studentdistributionbot.BotState;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.UserContextStorage;
import org.example.studentdistributionbot.client.apply_controller.PostApplyClient;
import org.example.studentdistributionbot.commands.apply_controller.PostApplyCommandHandler;
import org.example.studentdistributionbot.dto.PracticeApplicationRequest;
import org.example.studentdistributionbot.dto.PracticeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostApplyCommandHandlerTest {

    @Mock
    private UserContextStorage userContextStorage;

    @Mock
    private PostApplyClient postApplyClient;

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private Update update;

    @Mock
    private Message message;

    private PostApplyCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PostApplyCommandHandler(userContextStorage, postApplyClient);
    }

    @Test
    void shouldReturnCorrectCommand() {
        assertThat(handler.getCommand()).isEqualTo(Command.POST_APPLY);
    }

    @Test
    void shouldHandleCommand() throws TelegramApiException {
        Long chatId = 123L;
        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);

        handler.handleCommand(update, telegramClient);

        verify(userContextStorage).setState(chatId, BotState.WAITING_FOR_PRACTICE_APPLICATION);
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().contains("Введите ИНН") &&
                        msg.getChatId().equals(chatId.toString())));
    }

    @Test
    void shouldPostApplySuccessfully() throws TelegramApiException {
        Long chatId = 123L;
        PracticeApplicationRequest request = createTestRequest();
        String successMessage = "Заявка успешно создана";

        when(postApplyClient.postApply(request)).thenReturn(successMessage);

        handler.postApply(request, telegramClient, chatId);

        verify(postApplyClient).postApply(request);
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().equals(successMessage) &&
                        msg.getChatId().equals(chatId.toString())));
    }

    @Test
    void shouldHandleErrorWhenPostingApply() throws TelegramApiException {
        Long chatId = 123L;
        PracticeApplicationRequest request = createTestRequest();
        String errorMessage = "Ошибка при создании заявки";

        when(postApplyClient.postApply(request)).thenReturn(errorMessage);

        handler.postApply(request, telegramClient, chatId);

        verify(postApplyClient).postApply(request);
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().equals(errorMessage) &&
                        msg.getChatId().equals(chatId.toString())));
    }

    private PracticeApplicationRequest createTestRequest() {
        return PracticeApplicationRequest.builder()
                .inn(1234567890l)
                .organisationName("Test Organization")
                .location("Test City")
                .supervisorName("Test Supervisor")
                .mail("test@example.com")
                .phone("+71234567890")
                .practiceType(PracticeType.ONLINE)
                .build();
    }
}