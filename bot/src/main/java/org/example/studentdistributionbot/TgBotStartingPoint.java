package org.example.studentdistributionbot;

import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.client.ClientFacade;
import org.example.studentdistributionbot.commands.BotCommandHandler;
import org.example.studentdistributionbot.commands.LoadApproveFileHandler;
import org.example.studentdistributionbot.commands.aply_controller.PostApplyCommandHandler;
import org.example.studentdistributionbot.commands.approvalStatusController.GetApprovalsCommandHandler;
import org.example.studentdistributionbot.commands.approvalStatusController.GetApprovalsStudentStatusCommandHandler;
import org.example.studentdistributionbot.commands.approvalStatusController.PostApprovalsExcelCommandHandler;
import org.example.studentdistributionbot.commands.approvalStatusController.PutApprovalsIsuNumberHandler;
import org.example.studentdistributionbot.dto.*;
import org.example.studentdistributionbot.file.BotFileHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TgBotStartingPoint implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final UserContextStorage userContextStorage;
    private final ClientFacade clientFacade;
    private final BotFileHandler botFileHandler;
    private final Map<Command, BotCommandHandler> commandHandlers;

    @Value("${telegram-bot.token}")
    private String tgBotToken;

    public TgBotStartingPoint(@Value("${telegram-bot.token}") String tgBotToken,
                              List<BotCommandHandler> handlers, UserContextStorage userContextStorage,
                              ClientFacade clientFacade, BotFileHandler botFileHandler) {
        this.tgBotToken = tgBotToken;
        this.telegramClient = new OkHttpTelegramClient(getBotToken());
        this.userContextStorage = userContextStorage;
        this.clientFacade = clientFacade;
        this.botFileHandler = botFileHandler;
        this.commandHandlers = handlers.stream().collect(Collectors.toMap(BotCommandHandler::getCommand, c -> c));
    }

    @Override
    public String getBotToken() {
        return this.tgBotToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            UserMetadata userMetadata = buildUserMetadata(update);
            log.info("Message received : '{}' from {}", userMetadata.messageText, userMetadata);

            if (update.getMessage().hasText()) {
                var command = userMetadata.messageText.replace("/", "").split("\\s+")[0];
                if (command.equalsIgnoreCase(Command.CANCEL.getValue())) {
                    userContextStorage.clear(userMetadata.chatId);
                }
            }

            BotState userState = userContextStorage.getState(userMetadata.chatId);

            switch (userState) {
                case WAITING_FOR_ISU_NUMBER -> {
                    String isuNumber = update.getMessage().getText();
                    var registerDto = userContextStorage.getData(userMetadata.chatId);
                    registerDto.setIsuNumber(isuNumber);
                    registerDto.setTelegramUsername(userMetadata.username);
                    registerDto.setTelegramId(userMetadata.telegramId);
                    userContextStorage.setState(userMetadata.chatId, BotState.WAITING_FOR_FULL_NAME);
                    sendMessage(userMetadata.chatId, "Введите ФИО");
                }
                case WAITING_FOR_PRACTICE_APPLICATION -> {
                    String text = update.getMessage().getText();
                    String[] values = text.split("\\r?\\n");
                    PracticeApplicationRequest practiceApplicationRequest = new PracticeApplicationRequest();
                    practiceApplicationRequest.setTelegramId(userMetadata.telegramId);
                    practiceApplicationRequest.setInn(Long.parseLong(values[0]));
                    practiceApplicationRequest.setOrganisationName(values[1]);
                    practiceApplicationRequest.setLocation(values[2]);
                    practiceApplicationRequest.setSupervisorName(values[3]);
                    practiceApplicationRequest.setMail(values[4]);
                    practiceApplicationRequest.setPhone(values[5]);
                    practiceApplicationRequest.setPracticeType(PracticeType.fromValue(values[6]));

                    PostApplyCommandHandler commandHandler = (PostApplyCommandHandler) commandHandlers.get(Command.POST_APPLY);
                    try {
                        commandHandler.postApply(practiceApplicationRequest, telegramClient, userMetadata.chatId);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        sendMessage(userMetadata.chatId, e.getMessage());
                    }
                }
                case WAITING_FOR_FULL_NAME -> {
                    userContextStorage.getData(userMetadata.chatId).setFullName(userMetadata.messageText);
                    var response = clientFacade.registerUser(userContextStorage.getData(userMetadata.chatId));
                    if (response != null) {
                        userContextStorage.clear(userMetadata.chatId);
                        sendMessage(userMetadata.chatId, response);
                    } else {
                        sendMessage(userMetadata.chatId, "Произошла ошибка при регистрации");
                    }
                }
                case WAITING_FOR_APPROVE_FILE_LOADING -> {
                    if (update.getMessage().hasDocument()) {
                        Document doc = update.getMessage().getDocument();
                        String fileId = doc.getFileId();
                        String fileName = doc.getFileName();
                        try (var stream = botFileHandler.downloadTelegramFileStream(fileId, telegramClient, tgBotToken)) {
                            LoadApproveFileHandler commandHandler = (LoadApproveFileHandler) commandHandlers.get(Command.LOAD_FILE_APPROVE);
                            commandHandler.loadFile(fileName, stream, telegramClient, userMetadata.chatId);
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    }
                    userContextStorage.clear(userMetadata.chatId);
                }
                case WAITING_FOR_APPROVE_EXCEL -> {
                    if (update.getMessage().hasDocument()) {
                        Document doc = update.getMessage().getDocument();
                        String fileId = doc.getFileId();
                        String fileName = doc.getFileName();
                        try (var stream = botFileHandler.downloadTelegramFileStream(fileId, telegramClient, tgBotToken)) {
                            PostApprovalsExcelCommandHandler commandHandler = (PostApprovalsExcelCommandHandler) commandHandlers.get(Command.POST_APPROVALS_EXCEL);
                            commandHandler.loadFile(fileName, stream, telegramClient, userMetadata.chatId);
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    }
                    userContextStorage.clear(userMetadata.chatId);
                }
                case WAITING_FOR_GROUP_NAME_AND_STATUS -> {
                    String text = update.getMessage().getText();
                    GetApprovalsDto getApprovalsDto = new GetApprovalsDto();
                    String[] parts = text.split(" ", 2);
                    String firstPart = parts[0];
                    String secondPart = parts.length > 1 ? parts[1] : null;
                    getApprovalsDto.setStatus(firstPart);
                    getApprovalsDto.setStudyGroupName(secondPart);
                    GetApprovalsCommandHandler commandHandler = (GetApprovalsCommandHandler) commandHandlers.get(Command.GET_APPROVALS);
                    try {
                        commandHandler.getApprovals(getApprovalsDto, telegramClient, userMetadata.chatId);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                    userContextStorage.clear(userMetadata.chatId);
                }
                case WAITING_FOR_ISU_NUMBER_AND_STATUS -> {
                    String text = update.getMessage().getText();
                    String[] parts = text.split(" ", 2);
                    String isuNumber = parts[0];
                    String secondPart = parts[1];
                    ApprovalStatusDTO approvalStatusDTO = new ApprovalStatusDTO(secondPart);
                    PutApprovalsIsuNumberHandler commandHandler = (PutApprovalsIsuNumberHandler) commandHandlers.get(Command.PUT_APPROVALS);
                    try {
                        commandHandler.putApprovals(approvalStatusDTO, isuNumber, telegramClient, userMetadata.chatId);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                    userContextStorage.clear(userMetadata.chatId);
                }
                case WAITING_FOR_ISU_NUMBER_FOR_STUDENT_STATUS -> {
                    String isuNumber = update.getMessage().getText();

                    GetApprovalsStudentStatusCommandHandler commandHandler = (GetApprovalsStudentStatusCommandHandler) commandHandlers.get(Command.GET_STUDENT_STATUS);
                    try {
                        commandHandler.getStudentStatus(isuNumber, telegramClient, userMetadata.chatId);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                    userContextStorage.clear(userMetadata.chatId);
                }
                case IDLE -> {
                    if (userMetadata.messageText.startsWith("/")) {
                        String command = userMetadata.messageText.replace("/", "").split("\\s+")[0];
                        try {
                            var commandHandler = commandHandlers.get(Command.fromValue(command));
                            commandHandler.handleCommand(update, telegramClient);
                        } catch (IllegalArgumentException e) {
                            log.error(e.getMessage());
                            sendMessage(userMetadata.chatId, "Неизвестная команда /%s".formatted(command));
                        } catch (Exception e) {
                            log.error(e.getMessage());
                            sendMessage(userMetadata.chatId, "Неизвестная ошибка");
                        }
                    }
                }
            }
        }

    }

    private void sendMessage(Long chatId, String message) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private UserMetadata buildUserMetadata(Update update) {
        return new UserMetadata(update.getMessage().getFrom().getUserName(),
                update.getMessage().getFrom().getId(), update.getMessage().getChatId(),
                update.getMessage().getText(), clientFacade.getUserRole(update.getMessage().getFrom().getId()));
    }

    record UserMetadata(String username, Long telegramId, Long chatId, String messageText, UserRole userRole) {

    }
}
