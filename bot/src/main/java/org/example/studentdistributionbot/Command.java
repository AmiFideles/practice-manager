package org.example.studentdistributionbot;

import lombok.Getter;

@Getter
public enum Command {
    GET_APPROVALS("get_approvals", true, "Получить студентов по статусу регистрации\n"),
    GET_APPROVALS_EXCEL("get_approvals_excel", true, "Скачать шаблон для подтверждения\n"),
    GET_EXCEL_REPORT("get_excel_report", true, "Скачать отчет по студентам\n"),
    POST_APPROVALS_EXCEL("post_approvals_excel", true, "Загрузить подтверждения\n"),
    PUT_APPROVALS("put_approvals", true, "Изменить статус регистрации по ISU номеру\n"),
    GET_STUDENT_STATUS("get_student_status", true, "Получить статус регистрации студента\n"),
    GET_STUDENT_STATUS_ISU_NUMBER("get_student_status_isu_number", true, "Получить студента по его ИСУ\n"),
    LOAD_FILE_APPROVE("load_file", true, "Загрузить файл со студентами\n"),
    REGISTER("register", false, "Регистрация студента\n"),
    START("start", false, "Стратовая команда\n"),
    HELP("help", false, "Помощь\n"),
    APPROVE_STUDENT("approve", true, "Аппрувнуть регистрацию студента.\nПример - /approve {isuNumber}\n"),
    CANCEL("cancel", false, "Сбросить состояние бота, выйти из команды\n"),
    GET_REPORT("students_report", true, "Получить файл со студентами\n"),
    REJECT("reject", true, "Отказать регистрации студента.\nПример - /reject {isuNumber}\n"),

    // apply-controller
    SET_REQUEST_STATUS("set_request_status", true, """
            Установить студенту статус его заявки по ISU номеру.
            Пример - /set_request_status {isuNumber} PENDING/APPROVED/REJECTED
            """),
    POST_APPLY("post_apply", false, "Создать заявку\n"),
    GET_APPLY("get_apply", true, "Получить заявки по фильтрам\n"),
    GET_APPLY_PDF("get_apply_pdf", false, "Получить pdf\n"),

    GET_STUDENTS("get_students", true, "Получить выборку студентов по фильтрам\n");


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
