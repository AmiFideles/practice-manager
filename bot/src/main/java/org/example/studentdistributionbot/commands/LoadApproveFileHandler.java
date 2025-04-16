package org.example.studentdistributionbot.commands;

import lombok.RequiredArgsConstructor;
import org.example.studentdistributionbot.BotState;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.UserContextStorage;
import org.example.studentdistributionbot.client.ApproveFileLoadingClient;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class LoadApproveFileHandler implements BotCommandHandler {
    private final UserContextStorage userContextStorage;
    private final UserRoleResolverClient userRoleResolverClient;
    private final ApproveFileLoadingClient approveFileLoadingClient;

    @Override
    public Command getCommand() {
        return Command.LOAD_FILE_APPROVE;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        var userRole = userRoleResolverClient.getUserRole(update.getMessage().getFrom().getId());
        if (!userRole.getRole().equals("ADMIN")) {
            return;
        }
        Long chatId = update.getMessage().getChatId();
        userContextStorage.setState(chatId, BotState.WAITING_FOR_APPROVE_FILE_LOADING);
        sendMessage(chatId, client, "Загрузите файл");
    }

    public void loadFile(String fileName, InputStream stream, TelegramClient client, Long chatId) throws TelegramApiException {
        HttpStatusCode statusCode = approveFileLoadingClient.uploadFile(fileName, stream);
        if (statusCode.is2xxSuccessful()) {
            sendMessage(chatId, client, "Файл успешно загружен");
        } else {
            sendMessage(chatId, client, "Что-то пошло не так");
        }
    }
}
