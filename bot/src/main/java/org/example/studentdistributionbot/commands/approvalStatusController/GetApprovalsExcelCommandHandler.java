package org.example.studentdistributionbot.commands.approvalStatusController;

import lombok.RequiredArgsConstructor;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.client.approvalStatusController.GetApprovalsExcelClient;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.example.studentdistributionbot.commands.BotCommandHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class GetApprovalsExcelCommandHandler implements BotCommandHandler {
    private final UserRoleResolverClient userRoleResolverClient;
    private final GetApprovalsExcelClient getApprovalsExcelClient;

    @Override
    public Command getCommand() {
        return Command.GET_APPROVALS_EXCEL;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        try {
            var userRole = userRoleResolverClient.getUserRole(update.getMessage().getFrom().getId());
            if (!userRole.getRole().equals("ADMIN")) {
                return;
            }
            byte[] excelData = getApprovalsExcelClient.getApprovalsExcel();


            if (excelData == null || excelData.length == 0) {
                throw new IllegalStateException("Получен пустой Excel файл");
            }

            Path tempFile = Files.createTempFile("approvals_", ".xlsx");
            try {
                Files.write(tempFile, excelData);

                SendDocument sendDocument = new SendDocument(update.getMessage().getChatId().toString(), new InputFile(tempFile.toFile(), "approvals_report.xlsx"));

                client.execute(sendDocument);
            } finally {
                Files.deleteIfExists(tempFile);
            }

        } catch (Exception e) {
        }
    }
}
