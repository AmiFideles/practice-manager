package org.example.studentdistributionbot;

import lombok.Getter;

@Getter
public enum Command {
    GET_APPROVALS("get_approvals", true, "Получить студентов по статусу регистрации"),
    GET_APPROVALS_EXCEL("get_approvals_excel", true, "Скачать шаблон для подтверждения"),
    POST_APPROVALS_EXCEL("post_approvals_excel", true, "Загрузить подтверждения"),
    PUT_APPROVALS("put_approvals", true, "Изменить статус регистрации по ISU номеру"),
    GET_STUDENT_STATUS("get_student_status", true, "Получить статус регистрации студента"),
    LOAD_FILE_APPROVE("load_file", true, "Загрузить файл со студентами"),
    REGISTER("register", false, "Регистрация студента"),
    START("start", false, "Стратовая команда"),
    HELP("help", false, "Помощь"),
    APPORVE_STUDENT("approve", true, "Аппрувнуть регистрацию студента. `/approve {isuNumber}`"),
    CANCEL("cancel", false, "Сбросить состояние бота"),
    GET_REPORT("students_report", true, "Получить файл со студентами"),
    REJECT("reject", true, "Отказать регистрации студента. `/reject {isuNumber}`");


    private final String value;
    private final Boolean isAdminCommand;
    private final String description;


    Command(String value, Boolean isAdminCommand, String description) {
        this.value = value;
        this.isAdminCommand = isAdminCommand;
        this.description = description;
    }

    public static Command fromValue(String value) {
        for (Command command : Command.values()) {
            if (command.getValue().equalsIgnoreCase(value)) {
                return command;
            }
        }
        throw new IllegalArgumentException("Unknown command: " + value);
    }

}
