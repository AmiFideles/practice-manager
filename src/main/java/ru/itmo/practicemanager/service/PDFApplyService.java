package ru.itmo.practicemanager.service;

import org.springframework.stereotype.Service;

@Service
public class PDFApplyService {

    public byte[] generatePracticeApplicationPdf(String tgUsername) {
        // Логика генерации PDF будет здесь
        // 1. Поиск данных студента и заявки по tgUsername
        // 2. Формирование PDF документа
        // 3. Возврат PDF в виде byte[]
        return new byte[0];
    }
}
