package org.example.studentdistributionbot.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.InputStream;
import java.net.URL;

@Component
@Slf4j
public class BotFileHandler {
    public InputStream downloadTelegramFileStream(String fileId, TelegramClient telegramClient, String botToken) {
        try {
            // Получаем информацию о файле
            GetFile getFileMethod = new GetFile(fileId);
            File file = telegramClient.execute(getFileMethod);

            String filePath = file.getFilePath();
            String fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + filePath;

            return new URL(fileUrl).openStream();

        } catch (Exception e) {
            log.error("Ошибка загрузки файла по fileId: " + fileId, e);
            return null;
        }
    }

}
