package org.example.studentdistributionbot.commands.approvalStatusController;

import lombok.RequiredArgsConstructor;
import org.example.studentdistributionbot.BotState;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.UserContextStorage;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.example.studentdistributionbot.client.approvalStatusController.GetApprovalsClient;
import org.example.studentdistributionbot.commands.BotCommandHandler;
import org.example.studentdistributionbot.dto.GetApprovalsDto;
import org.example.studentdistributionbot.dto.GroupResponseDto;
import org.example.studentdistributionbot.dto.StudentDto;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetApprovalsCommandHandler implements BotCommandHandler {
    private final UserContextStorage userContextStorage;
    private final UserRoleResolverClient userRoleResolverClient;
    private final GetApprovalsClient getApprovalsClient;

    @Override
    public Command getCommand() {
        return Command.GET_APPROVALS;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        var userRole = userRoleResolverClient.getUserRole(update.getMessage().getFrom().getId());
        if (!userRole.getRole().equals("ADMIN")) {
            return;
        }
        Long chatId = update.getMessage().getChatId();
        userContextStorage.setState(chatId, BotState.WAITING_FOR_GROUP_NAME_AND_STATUS);
        sendMessage(chatId, client, "Введите один из статусов: NOT_REGISTERED, WAITING_FOR_APPROVAL, REGISTERED, REJECTED; и номер группы(не обязательно)");
    }

    public void getApprovals(GetApprovalsDto getApprovalsDto, TelegramClient client, Long chatId) throws TelegramApiException {
        var response = getApprovalsClient.getApprovals(getApprovalsDto);
        var message = beautify(response);
        sendMessage(chatId, client, message);
    }

    private String beautify(List<GroupResponseDto> groups) {
        StringBuilder result = new StringBuilder();
        for (GroupResponseDto group : groups) {
            result.append("Группа: ").append(group.getGroupNumber()).append("\n");
            for (StudentDto student : group.getStudents()) {
                result.append("- ").append(student.getFullName())
                        .append(" (ISU: ").append(student.getIsuNumber()).append(")\n");
            }
            result.append("\n");
        }
        return result.toString();
    }

}
