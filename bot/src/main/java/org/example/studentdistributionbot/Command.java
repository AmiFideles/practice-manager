package org.example.studentdistributionbot;

import lombok.Getter;

@Getter
public enum Command {
    GET_APPROVALS("get_approvals", true, "Получить студентов по статусу регистрации\n"),
    GET_APPROVALS_EXCEL("get_approvals_excel", true, "Скачать шаблон для подтверждения\n"),
    POST_APPROVALS_EXCEL("post_approvals_excel", true, "Загрузить подтверждения\n"),
    PUT_APPROVALS("put_approvals", true, "Изменить статус регистрации по ISU номеру\n"),
    GET_STUDENT_STATUS("get_student_status", true, "Получить статус регистрации студента\n"),
    LOAD_FILE_APPROVE("load_file", true, "Загрузить файл со студентами\n"),
    REGISTER("register", false, "Регистрация студента\n"),
    START("start", false, "Стратовая команда\n"),
    HELP("help", false, "Помощь\n"),
    APPROVE_STUDENT("approve", true, "Аппрувнуть регистрацию студента. /approve {isuNumber}\n"),
    CANCEL("cancel", false, "Сбросить состояние бота\n"),
    GET_REPORT("students_report", true, "Получить файл со студентами\n"),
    REJECT("reject", true, "Отказать регистрации студента. /reject {isuNumber}\n"),
    SET_REQUEST_STATUS("set_request_status", true, "Установить студенту статус его заявки по ISU номеру\n");


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
