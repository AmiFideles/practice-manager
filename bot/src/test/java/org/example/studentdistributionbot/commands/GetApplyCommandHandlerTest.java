package org.example.studentdistributionbot.commands;

import org.example.studentdistributionbot.BotState;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.UserContextStorage;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.example.studentdistributionbot.client.apply_controller.GetApplyClient;
import org.example.studentdistributionbot.commands.apply_controller.GetApplyCommandHandler;
import org.example.studentdistributionbot.dto.ApplyResponseDto;
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
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetApplyCommandHandlerTest {

    @Mock
    private GetApplyClient getApplyClient;

    @Mock
    private UserContextStorage userContextStorage;

    @Mock
    private UserRoleResolverClient userRoleResolverClient;

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private User user;

    private GetApplyCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetApplyCommandHandler(getApplyClient, userContextStorage, userRoleResolverClient);
    }

    @Test
    void shouldReturnCorrectCommand() {
        assertThat(handler.getCommand()).isEqualTo(Command.GET_APPLY);
    }

    @Test
    void shouldHandleCommandForAdmin() throws Exception {
        Long chatId = 123L;
        Long userId = 456L;
        UserRole adminRole = new UserRole("ADMIN");

        when(update.getMessage()).thenReturn(message);
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(chatId);
        when(user.getId()).thenReturn(userId);
        when(userRoleResolverClient.getUserRole(userId)).thenReturn(adminRole);

        handler.handleCommand(update, telegramClient);

        verify(userContextStorage).setState(chatId, BotState.WAITING_APPLY_FILTERS);
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().contains("Введите фильтры") &&
                        msg.getChatId().equals(chatId.toString())));
    }

    @Test
    void shouldNotHandleCommandForNonAdmin() throws Exception {
        Long userId = 456L;
        UserRole userRole = new UserRole("USER");

        when(update.getMessage()).thenReturn(message);
        when(message.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(userRoleResolverClient.getUserRole(userId)).thenReturn(userRole);

        handler.handleCommand(update, telegramClient);

        verify(userContextStorage, never()).setState(any(), any());
        verify(telegramClient, never()).execute(any(SendMessage.class));
    }

    @Test
    void shouldHandleRequestWithNoFilters() throws Exception {
        Long chatId = 123L;
        String[] filters = new String[]{};
        List<ApplyResponseDto> mockResponse = createMockResponses();

        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(getApplyClient.getApplies(null, null, null)).thenReturn(mockResponse);

        handler.doRequest(update, telegramClient, filters);

        verify(getApplyClient).getApplies(null, null, null);
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().contains("Список заявок")));
    }

    @Test
    void shouldHandleRequestWithOneFilter() throws Exception {
        Long chatId = 123L;
        String[] filters = new String[]{"PENDING"};
        List<ApplyResponseDto> mockResponse = createMockResponses();

        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(getApplyClient.getApplies("PENDING", null, null)).thenReturn(mockResponse);

        handler.doRequest(update, telegramClient, filters);

        verify(getApplyClient).getApplies("PENDING", null, null);
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().contains("Список заявок")));
    }

    @Test
    void shouldHandleRequestWithTwoFilters() throws Exception {
        Long chatId = 123L;
        String[] filters = new String[]{"PENDING", "GROUP1"};
        List<ApplyResponseDto> mockResponse = createMockResponses();

        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(getApplyClient.getApplies("PENDING", "GROUP1", null)).thenReturn(mockResponse);

        handler.doRequest(update, telegramClient, filters);

        verify(getApplyClient).getApplies("PENDING", "GROUP1", null);
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().contains("Список заявок")));
    }

    @Test
    void shouldHandleRequestWithThreeFilters() throws Exception {
        Long chatId = 123L;
        String[] filters = new String[]{"PENDING", "GROUP1", "12345"};
        List<ApplyResponseDto> mockResponse = createMockResponses();

        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(getApplyClient.getApplies("PENDING", "GROUP1", "12345")).thenReturn(mockResponse);

        handler.doRequest(update, telegramClient, filters);

        verify(getApplyClient).getApplies("PENDING", "GROUP1", "12345");
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().contains("Список заявок")));
    }

    @Test
    void shouldHandleRequestWithTooManyFilters() throws Exception {
        Long chatId = 123L;
        String[] filters = new String[]{"PENDING", "GROUP1", "12345", "EXTRA"};

        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);

        handler.doRequest(update, telegramClient, filters);

        verify(getApplyClient, never()).getApplies(any(), any(), any());
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().contains("Введите правильно фильтры")));
    }

    @Test
    void shouldHandleEmptyResponse() throws Exception {
        Long chatId = 123L;
        String[] filters = new String[]{};

        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(getApplyClient.getApplies(null, null, null)).thenReturn(List.of());

        handler.doRequest(update, telegramClient, filters);

        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getText().contains("Ничего не найдено")));
    }

    private List<ApplyResponseDto> createMockResponses() {
        ApplyResponseDto dto = new ApplyResponseDto();
        dto.setId(1L);
        dto.setStatus("PENDING");
        dto.setCheckStatus("CHECKED");
        dto.setIsuNumber("12345");
        dto.setStudentName("Test Student");
        dto.setGroupNumber("GROUP1");
        dto.setOrganisationName("Test Org");
        dto.setLocation("Test Location");
        dto.setSupervisorName("Test Supervisor");
        dto.setMail("test@test.com");
        dto.setPhone("1234567890");
        dto.setPracticeType("Test Practice");

        return List.of(dto);
    }
}