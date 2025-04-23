package ru.itmo.practicemanager.service.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ru.itmo.practicemanager.dto.TemplateOption;

import java.io.File;
import java.io.IOException;

@Service
public class TemplateOptionService {
    private final ObjectMapper objectMapper;
    private final File templateFile;

    public TemplateOptionService(ObjectMapper objectMapper) throws IOException {
        this.objectMapper = objectMapper;
        ClassPathResource resource = new ClassPathResource("template_option.json");
        this.templateFile = resource.getFile();
    }

    /**
     * Устанавливает строку даты практики в JSON-файле
     */
    public synchronized void setPracticeDate(String date) {
        try {
            TemplateOption option = objectMapper.readValue(templateFile, TemplateOption.class);
            option.setDate(date);
            objectMapper.writeValue(templateFile, option);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось записать дату практики", e);
        }
    }

    /**
     * Считывает строку даты практики из JSON-файла
     */
    public String getPracticeDate() {
        try {
            TemplateOption option = objectMapper.readValue(templateFile, TemplateOption.class);
            return option.getDate();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать дату практики", e);
        }
    }
}