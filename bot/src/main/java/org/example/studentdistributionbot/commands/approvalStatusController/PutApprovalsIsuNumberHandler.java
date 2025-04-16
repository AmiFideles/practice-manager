package org.example.studentdistributionbot.commands.approvalStatusController;

import lombok.RequiredArgsConstructor;
import org.example.studentdistributionbot.BotState;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.UserContextStorage;
import org.example.studentdistributionbot.client.approvalStatusController.PutApprovalsIsuNumberClient;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.example.studentdistributionbot.commands.BotCommandHandler;
import org.example.studentdistributionbot.dto.ApprovalStatusDTO;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class PutApprovalsIsuNumberHandler implements BotCommandHandler {
    private final UserContextStorage userContextStorage;
    private final UserRoleResolverClient userRoleResolverClient;
    private final PutApprovalsIsuNumberClient putApprovalsIsuNumberClient;
    @Override
    public Command getCommand() {
        return Command.PUT_APPROVALS;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        var userRole = userRoleResolverClient.getUserRole(update.getMessage().getFrom().getId());
        if (!userRole.getRole().equals("ADMIN")) {
            return;
        }
        Long chatId = update.getMessage().getChatId();
        userContextStorage.setState(chatId, BotState.WAITING_FOR_ISU_NUMBER_AND_STATUS);
        sendMessage(chatId, client, "Введите номер ИСУ и один из статусов: NOT_REGISTERED, WAITING_FOR_APPROVAL, REGISTERED, REJECTED)");
    }

    public void putApprovals(ApprovalStatusDTO approvalStatusDTO, String isuNumber,  TelegramClient client, Long chatId) throws TelegramApiException {
        String message = putApprovalsIsuNumberClient.putApprovals(approvalStatusDTO, isuNumber);
        sendMessage(chatId, client, message);
    }
}
