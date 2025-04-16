package org.example.studentdistributionbot.commands.student_controller;

import lombok.RequiredArgsConstructor;
import org.example.studentdistributionbot.BotState;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.UserContextStorage;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.example.studentdistributionbot.client.approvalStatusController.GetApprovalsStudentStatusClient;
import org.example.studentdistributionbot.client.student_controller.GetStudentIsuNumberClient;
import org.example.studentdistributionbot.commands.BotCommandHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class GetStudentsIsuNumberHandler implements BotCommandHandler {
    private final UserContextStorage userContextStorage;
    private final UserRoleResolverClient userRoleResolverClient;
    private final GetStudentIsuNumberClient getStudentIsuNumberClient;
    @Override
    public Command getCommand() {
        return Command.GET_STUDENT_STATUS_ISU_NUMBER;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        var userRole = userRoleResolverClient.getUserRole(update.getMessage().getFrom().getId());
        if (!userRole.getRole().equals("ADMIN")) {
            return;
        }
        Long chatId = update.getMessage().getChatId();
        userContextStorage.setState(chatId, BotState.WAITING_FOR_ISU_NUMBER_FOR_GET_STUDENT_STATUS);
        sendMessage(chatId, client, "Введите номер ИСУ студента");
    }

    public void getStudentIsuNumber(String isuNumber, TelegramClient client, Long chatId) throws TelegramApiException {
        String message = getStudentIsuNumberClient.getStudentIsuNumber(isuNumber);
        sendMessage(chatId, client, message);
    }
}
