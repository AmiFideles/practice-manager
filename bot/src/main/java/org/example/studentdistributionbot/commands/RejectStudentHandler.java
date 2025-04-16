package org.example.studentdistributionbot.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.client.RegisterStudentClient;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.example.studentdistributionbot.dto.ApprovalStatusDTO;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class RejectStudentHandler implements BotCommandHandler {

    private final RegisterStudentClient registerStudentClient;
    private final UserRoleResolverClient userRoleResolverClient;


    @Override
    public Command getCommand() {
        return Command.REJECT;
    }

    // copy-paste ApproveStudentHandler

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        var userRole = userRoleResolverClient.getUserRole(update.getMessage().getFrom().getId());
        if (!userRole.getRole().contains("ADMIN")) {
            return;
        }
        String[] message = update.getMessage().getText().split(" ");
        if (message.length != 2) {
            sendMessage(update.getMessage().getChatId(), client, "Введите команду правильно, /reject {isuNumber}");
            return;
        }
        var response = registerStudentClient.changeStatusStudent(message[1], new ApprovalStatusDTO("REJECTED"));
        sendMessage(update.getMessage().getChatId(), client, response);
    }
}
