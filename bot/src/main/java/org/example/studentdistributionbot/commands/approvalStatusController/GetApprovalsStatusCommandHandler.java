package org.example.studentdistributionbot.commands.approvalStatusController;

import lombok.RequiredArgsConstructor;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.client.approvalStatusController.GetApprovalsStatusClient;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.example.studentdistributionbot.commands.BotCommandHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class GetApprovalsStatusCommandHandler implements BotCommandHandler {
    private final UserRoleResolverClient userRoleResolverClient;
    private final GetApprovalsStatusClient getApprovalsStatusClient;
    @Override
    public Command getCommand() {
        return Command.GET_APPROVALS_STATUS;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        var userRole = userRoleResolverClient.getUserRole(update.getMessage().getFrom().getId());
        if (!userRole.getRole().equals("ADMIN")) {
            return;
        }
        Long chatId = update.getMessage().getChatId();
        String message = getApprovalsStatusClient.getApprovalsStatus();
        sendMessage(chatId, client, message);
    }
}
