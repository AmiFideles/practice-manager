package org.example.studentdistributionbot.commands.student_controller;

import lombok.RequiredArgsConstructor;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.client.student_controller.StudentsReportGetClient;
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
public class StudentsReportGetHandler implements BotCommandHandler {

    private final StudentsReportGetClient studentsReportGetClient;

    @Override
    public Command getCommand() {
        return Command.GET_REPORT;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        byte[] fileData = studentsReportGetClient.getExcelReport();

        InputFile inputFile = new InputFile(new ByteArrayInputStream(fileData), "students_report.xlsx");

        SendDocument sendDocument = SendDocument.builder()
                .chatId(update.getMessage().getChatId())
                .document(inputFile)
                .caption("Список студентов")
                .build();

        client.execute(sendDocument);
    }
}
