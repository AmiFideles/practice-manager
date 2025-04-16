package org.example.studentdistributionbot.commands.aply_controller;


import lombok.RequiredArgsConstructor;
import org.example.studentdistributionbot.BotState;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.UserContextStorage;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.example.studentdistributionbot.client.aply_controller.PostApplyClient;
import org.example.studentdistributionbot.client.approvalStatusController.GetApprovalsClient;
import org.example.studentdistributionbot.commands.BotCommandHandler;
import org.example.studentdistributionbot.dto.ApprovalStatusDTO;
import org.example.studentdistributionbot.dto.PracticeApplicationRequest;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class PostApplyCommandHandler implements BotCommandHandler {
    private final UserContextStorage userContextStorage;
    private final UserRoleResolverClient userRoleResolverClient;
    private final PostApplyClient postApplyClient;

    @Override
    public Command getCommand() {
        return Command.POST_APPLY;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {

        Long chatId = update.getMessage().getChatId();
        userContextStorage.setState(chatId, BotState.WAITING_FOR_PRACTICE_APPLICATION);
        sendMessage(chatId, client, "Введите ИНН ,Название организации, Город, ФИО руководителя, mail, телефон, тип практики ONLINE или OFFLINE, одним сообщением с переводом строки");
    }

    public void postApply(PracticeApplicationRequest practiceApplicationRequest, TelegramClient client, Long chatId) throws TelegramApiException {
        String message = postApplyClient.postApply(practiceApplicationRequest);
        sendMessage(chatId, client, message);
    }

}
