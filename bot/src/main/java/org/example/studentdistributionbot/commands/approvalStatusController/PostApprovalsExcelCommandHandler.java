package org.example.studentdistributionbot.commands.approvalStatusController;

import lombok.RequiredArgsConstructor;
import org.example.studentdistributionbot.BotState;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.UserContextStorage;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.example.studentdistributionbot.client.approvalStatusController.PostApprovalsExcelClient;
import org.example.studentdistributionbot.commands.BotCommandHandler;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class PostApprovalsExcelCommandHandler implements BotCommandHandler {
    private final UserContextStorage userContextStorage;
    private final UserRoleResolverClient userRoleResolverClient;
    private final PostApprovalsExcelClient postApprovalsExcelClient;

    @Override
    public Command getCommand() {
        return Command.POST_APPROVALS_EXCEL;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        var userRole = userRoleResolverClient.getUserRole(update.getMessage().getFrom().getId());
        if (!userRole.getRole().equals("ADMIN")) {
            return;
        }
        Long chatId = update.getMessage().getChatId();
        userContextStorage.setState(chatId, BotState.WAITING_FOR_APPROVE_EXCEL);
        sendMessage(chatId, client, "Загрузите файл");
    }

    public void loadFile(String fileName, InputStream stream, TelegramClient client, Long chatId) throws TelegramApiException {
        try {
            HttpStatusCode statusCode = postApprovalsExcelClient.postApprovalsExcel(fileName, stream);
            if (statusCode != null && statusCode.is2xxSuccessful()) {
                sendMessage(chatId, client, "Файл успешно загружен");
            } else {
                sendMessage(chatId, client, "Ошибка при загрузке файла: некорректный ответ от сервера");
            }
        } catch (Exception e) {
            sendMessage(chatId, client, "Произошла ошибка при загрузке файла: " + e.getMessage());
        }
    }

}
