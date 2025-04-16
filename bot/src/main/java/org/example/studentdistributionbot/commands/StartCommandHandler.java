package org.example.studentdistributionbot.commands;

import org.example.studentdistributionbot.Command;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class StartCommandHandler implements BotCommandHandler {
    @Override
    public Command getCommand() {
        return Command.START;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        String messageText = "Привет! Это стартовая команда.";
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(messageText)
                .build();
        client.execute(sendMessage);
    }
}
