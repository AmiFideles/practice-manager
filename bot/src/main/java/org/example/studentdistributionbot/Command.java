package org.example.studentdistributionbot;

import lombok.Getter;

@Getter
public enum Command {
    GET_APPROVALS("get_approvals", true, "1"),
    GET_APPROVALS_STATUS("get_approvals_status", true, "2"),
    GET_APPROVALS_EXCEL("get_approvals_excel", true, "3"),
    POST_APPROVALS_EXCEL("post_approvals_excel", true, "4"),
    PUT_APPROVALS("put_approvals", true, "5"),
    GET_STUDENT_STATUS("get_student_status", true, "6"),
    LOAD_FILE_APPROVE("load_file", true, "7"),
    REGISTER("register", false, "8"),
    START("start", false, "9"),
    HELP("help", false, "10"),
    APPORVE_STUDENT("approve", true, "11"),
    CANCEL("cancel", false, "12");


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
