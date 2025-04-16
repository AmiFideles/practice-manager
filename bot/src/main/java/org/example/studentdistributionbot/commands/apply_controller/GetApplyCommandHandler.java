package org.example.studentdistributionbot.commands.apply_controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.BotState;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.UserContextStorage;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.example.studentdistributionbot.client.apply_controller.GetApplyClient;
import org.example.studentdistributionbot.commands.BotCommandHandler;
import org.example.studentdistributionbot.dto.ApplyResponseDto;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetApplyCommandHandler implements BotCommandHandler {

    private final GetApplyClient getApplyClient;
    private final UserContextStorage userContextStorage;
    private final UserRoleResolverClient userRoleResolverClient;

    @Override
    public Command getCommand() {
        return Command.GET_APPLY;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        var role = userRoleResolverClient.getUserRole(update.getMessage().getFrom().getId());
        if (!role.getRole().equals("ADMIN")) {
            return;
        }
        userContextStorage.setState(update.getMessage().getChatId(), BotState.WAITING_APPLY_FILTERS);
        sendMessage(update.getMessage().getChatId(), client, """
                Введите фильтры {status}(PENDING/APPROVED/REJECTED) {groupName} {isuNumber}
                - если без фильтров
                """);
    }

    public void doRequest(Update update, TelegramClient client, String[] filters) throws TelegramApiException {
        List<ApplyResponseDto> response = null;
        if (filters.length == 0) {
            response = getApplyClient.getApplies(null, null, null);
        } else if (filters.length == 1) {
            response = getApplyClient.getApplies(filters[0], null, null);
        } else if (filters.length == 2) {
            response = getApplyClient.getApplies(filters[0], filters[1], null);
        } else if (filters.length == 3) {
            response = getApplyClient.getApplies(filters[0], filters[1], filters[2]);
        } else {
            sendMessage(update.getMessage().getChatId(), client, "Введите правильно фильтры: {status} {groupName} {isuNumber}");
        }
        sendMessage(update.getMessage().getChatId(), client, formatApplicationsAsText(response));
    }

    private String formatApplicationsAsText(List<ApplyResponseDto> applications) {
        if (applications == null || applications.isEmpty()) {
            return "❗️ Ничего не найдено по заданным параметрам.";
        }

        StringBuilder sb = new StringBuilder("📋 Список заявок:\n\n");

        for (ApplyResponseDto app : applications) {
            sb.append("ID: ").append(app.getId()).append("\n")
                    .append("Статус: ").append(app.getStatus()).append("\n")
                    .append("check status: ").append(app.getCheckStatus()).append("\n")
                    .append("ISU: ").append(app.getIsuNumber()).append("\n")
                    .append("ФИО: ").append(app.getStudentName()).append("\n")
                    .append("Группа: ").append(app.getGroupNumber()).append("\n")
                    .append("ИНН: ").append(app.getId()).append("\n")
                    .append("Организация: ").append(app.getOrganisationName()).append("\n")
                    .append("Локация: ").append(app.getLocation()).append("\n")
                    .append("Руководитель").append(app.getSupervisorName()).append("\n")
                    .append("Почта: ").append(app.getMail()).append("\n")
                    .append("Телефон: ").append(app.getPhone()).append("\n")
                    .append("Тип практики: ").append(app.getPracticeType()).append("\n")
                    .append("-----------------------------\n");
        }

        return sb.toString();
    }

}
