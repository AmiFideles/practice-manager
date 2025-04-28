package ru.itmo.practicemanager.service.docx;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class DocxTemplateService {

    // путь в контейнере, смонтированный volume'ом
    private static final Path TEMPLATE_PATH = Path.of("/app/config/template.docx");

    private byte[] templateBytes;

    @PostConstruct
    public void init() {
        try {
            templateBytes = Files.readAllBytes(TEMPLATE_PATH);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось загрузить шаблон docx из " + TEMPLATE_PATH, e);
        }
    }

    /**
     * Возвращает копию байтов шаблона.
     * Каждому клиенту — свой массив, чтобы параллельно модифицировать безопасно.
     */
    public synchronized byte[] getTemplateCopy() {
        return Arrays.copyOf(templateBytes, templateBytes.length);
    }
}
