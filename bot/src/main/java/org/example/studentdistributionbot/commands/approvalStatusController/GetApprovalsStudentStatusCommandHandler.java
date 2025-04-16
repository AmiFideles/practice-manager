package org.example.studentdistributionbot.commands.approvalStatusController;

import lombok.RequiredArgsConstructor;
import org.example.studentdistributionbot.BotState;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.UserContextStorage;
import org.example.studentdistributionbot.client.approvalStatusController.GetApprovalsStudentStatusClient;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.example.studentdistributionbot.commands.BotCommandHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class GetApprovalsStudentStatusCommandHandler implements BotCommandHandler {
    private final UserContextStorage userContextStorage;
    private final UserRoleResolverClient userRoleResolverClient;
    private final GetApprovalsStudentStatusClient getApprovalsStudentStatusClient;
    @Override
    public Command getCommand() {
        return Command.GET_STUDENT_STATUS;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        var userRole = userRoleResolverClient.getUserRole(update.getMessage().getFrom().getId());
        if (!userRole.getRole().equals("ADMIN")) {
            return;
        }
        Long chatId = update.getMessage().getChatId();
        userContextStorage.setState(chatId, BotState.WAITING_FOR_ISU_NUMBER_FOR_STUDENT_STATUS);
        sendMessage(chatId, client, "Введите номер ИСУ студента");
    }

    public void getStudentStatus(String isuNumber, TelegramClient client, Long chatId) throws TelegramApiException {
        String message = getApprovalsStudentStatusClient.getStudentStatus(isuNumber);
        sendMessage(chatId, client, message);
    }
}
