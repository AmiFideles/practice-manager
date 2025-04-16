package org.example.studentdistributionbot.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.client.RegisterStudentClient;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.example.studentdistributionbot.dto.ApprovalStatusDTO;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApproveStudentHandler implements BotCommandHandler {

    private final RegisterStudentClient registerStudentClient;
    private final UserRoleResolverClient userRoleResolverClient;

    @Override
    public Command getCommand() {
        return Command.APPROVE_STUDENT;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        var userRole = userRoleResolverClient.getUserRole(update.getMessage().getFrom().getId());
        if (!userRole.getRole().contains("ADMIN")) {
            return;
        }
        String[] message = update.getMessage().getText().split(" ");
        if (message.length != 2) {
            sendMessage(update.getMessage().getChatId(), client, "Введите команду правильно, /approve {isuNumber}");
            return;
        }
        var response = registerStudentClient.changeStatusStudent(message[1], new ApprovalStatusDTO("REGISTERED"));
        sendMessage(update.getMessage().getChatId(), client, response);
    }
}
