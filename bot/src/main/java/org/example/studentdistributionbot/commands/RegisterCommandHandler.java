package org.example.studentdistributionbot.commands;

import lombok.RequiredArgsConstructor;
import org.example.studentdistributionbot.BotState;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.UserContextStorage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class RegisterCommandHandler implements BotCommandHandler {
    private final UserContextStorage userContextStorage;

    @Override
    public Command getCommand() {
        return Command.REGISTER;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        var chatId = update.getMessage().getChatId();
        userContextStorage.setState(chatId, BotState.WAITING_FOR_ISU_NUMBER);
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text("Введите номер ису")
                .build();
        client.execute(sendMessage);
    }
}
