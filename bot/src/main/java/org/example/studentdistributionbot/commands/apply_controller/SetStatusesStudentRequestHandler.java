package org.example.studentdistributionbot.commands.apply_controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.client.apply_controller.SetStatusesStudentRequestClient;
import org.example.studentdistributionbot.commands.BotCommandHandler;
import org.example.studentdistributionbot.dto.ApplyStatus;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetStatusesStudentRequestHandler implements BotCommandHandler {

    private final SetStatusesStudentRequestClient setStatusesStudentRequestClient;

    @Override
    public Command getCommand() {
        return Command.SET_REQUEST_STATUS;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        String message = update.getMessage().getText();
        String[] tokens = message.split("\\s+");
        if (tokens.length != 3) {
            sendMessage(update.getMessage().getChatId(), client, "Введите команду правильно: /%s {isuNumber} PENDING/APPROVED/REJECTED".formatted(getCommand().getValue()));
            return;
        }

        var response = setStatusesStudentRequestClient.setStatus(ApplyStatus.fromValue(tokens[2]), tokens[1]);
        sendMessage(update.getMessage().getChatId(), client, response);
    }
}
