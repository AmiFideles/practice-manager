package org.example.studentdistributionbot.commands.student_controller;

import lombok.RequiredArgsConstructor;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.example.studentdistributionbot.client.approvalStatusController.GetApprovalsExcelClient;
import org.example.studentdistributionbot.client.student_controller.GetStudentReportClient;
import org.example.studentdistributionbot.commands.BotCommandHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
@Component
public class GetStudentReportHandler implements BotCommandHandler {
    private final UserRoleResolverClient userRoleResolverClient;
    private final GetStudentReportClient getStudentReportClient;

    @Override
    public Command getCommand() {
        return Command.GET_EXCEL_REPORT;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        try {
            var userRole = userRoleResolverClient.getUserRole(update.getMessage().getFrom().getId());
            if (!userRole.getRole().equals("ADMIN")) {
                return;
            }
            byte[] excelData = getStudentReportClient.getApprovalsExcel();


            if (excelData == null || excelData.length == 0) {
                throw new IllegalStateException("Получен пустой Excel файл");
            }

            Path tempFile = Files.createTempFile("approvals_", ".xlsx");
            try {
                Files.write(tempFile, excelData);

                SendDocument sendDocument = new SendDocument(update.getMessage().getChatId().toString(), new InputFile(tempFile.toFile(), "report.xlsx"));

                client.execute(sendDocument);
            } finally {
                Files.deleteIfExists(tempFile);
            }

        } catch (Exception e) {
        }
    }
}
