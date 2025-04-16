package ru.itmo.practicemanager.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ContactValidator {

    // Допускается международный формат (+7, 8), 10-11 цифр, возможны пробелы, дефисы, скобки
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+7|8)[\\s-]?\\(?\\d{3}\\)?[\\s-]?\\d{3}[\\s-]?\\d{2}[\\s-]?\\d{2}$"
    );

    // Допускаются буквы, цифры, точки, дефисы, подчеркивания в имени; домен с минимум 2 символами
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        return PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    public boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}