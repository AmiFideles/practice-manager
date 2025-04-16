package org.example.studentdistributionbot.commands.apply_controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.client.apply_controller.GetApplyPdfClient;
import org.example.studentdistributionbot.commands.BotCommandHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.ByteArrayInputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetApplyPdfCommandHandler implements BotCommandHandler {

    private final GetApplyPdfClient getApplyPdfClient;

    @Override
    public Command getCommand() {
        return Command.GET_APPLY_PDF;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        try {
            var pdf = getApplyPdfClient.getPdfForTelegramId(update.getMessage().getFrom().getId());
            sendPdfToUser(update.getMessage().getChatId(), client, pdf);
        } catch (Exception e) {
            log.error(e.getMessage());
            sendMessage(update.getMessage().getChatId(), client, "При формировании PDF что-то пошло не так");
        }
    }

    public void sendPdfToUser(Long chatId, TelegramClient client, byte[] pdf) throws TelegramApiException {
        InputFile inputFile = new InputFile(new ByteArrayInputStream(pdf), "apply.pdf");

        SendDocument doc = SendDocument.builder()
                .chatId(chatId)
                .document(inputFile)
                .caption("📄 Ваша заявка на практику")
                .build();

        client.execute(doc);
    }
}
