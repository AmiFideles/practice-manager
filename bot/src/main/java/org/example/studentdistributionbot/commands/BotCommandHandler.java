package org.example.studentdistributionbot.commands;

import org.example.studentdistributionbot.Command;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public interface BotCommandHandler {
    Command getCommand();

    void handleCommand(Update update, TelegramClient client) throws TelegramApiException;

    default void sendMessage(Long chatId, TelegramClient telegramClient, String message) throws TelegramApiException {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .build();
        telegramClient.execute(sendMessage);
    }
}
