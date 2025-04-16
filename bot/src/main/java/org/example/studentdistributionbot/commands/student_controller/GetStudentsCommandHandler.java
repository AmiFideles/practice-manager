package org.example.studentdistributionbot.commands.student_controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.BotState;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.UserContextStorage;
import org.example.studentdistributionbot.client.student_controller.GetStudentsClient;
import org.example.studentdistributionbot.commands.BotCommandHandler;
import org.example.studentdistributionbot.dto.StudentsResponseDto;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetStudentsCommandHandler implements BotCommandHandler {

    private final GetStudentsClient getStudentsClient;
    private final UserContextStorage userContextStorage;


    @Override
    public Command getCommand() {
        return Command.GET_STUDENTS;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        userContextStorage.setState(update.getMessage().getChatId(), BotState.WAITING_GET_STUDENTS_FILTERS);
        sendMessage(update.getMessage().getChatId(), client, """
                Введите фильтры поиска - {group} {isStatementDelivered} {isStatementSigned} {isStatementScanned} {isNotificationSent}
                - на каждый фильтр, если не нужен
                """);
    }

    public void getStudents(Update update, TelegramClient telegramClient) throws TelegramApiException {
        String message = update.getMessage().getText();
        GetStudentsFilters filters = handleFilters(message.split(" "), update.getMessage().getChatId(), telegramClient);

        var students = getStudentsClient.getStudents(filters);
        sendMessage(update.getMessage().getChatId(), telegramClient, formatStudentsAsText(students));

    }

    private GetStudentsFilters handleFilters(String[] filters, Long charId, TelegramClient telegramClient) throws TelegramApiException {
        if (filters.length != 5) {
            sendMessage(charId, telegramClient, "Фильтров должно быть 5");
        }
        String[] newFilters = new String[filters.length];
        for (int i = 0; i < filters.length; i++) {
            if (filters[i].equals("-")) {
                newFilters[i] = null;
            } else {
                newFilters[i] = filters[i];
            }
        }
        return new GetStudentsFilters(newFilters[0], newFilters[1] == null ? null : Boolean.parseBoolean(newFilters[1]),
                newFilters[2] == null ? null : Boolean.parseBoolean(newFilters[2]),
                newFilters[3] == null ? null : Boolean.parseBoolean(newFilters[3]),
                newFilters[4] == null ? null : Boolean.parseBoolean(newFilters[4]));
    }

    private String formatStudentsAsText(List<StudentsResponseDto> students) {
        if (students == null || students.isEmpty()) {
            return "❗️ Ничего не найдено по заданным параметрам.";
        }
        StringBuilder sb = new StringBuilder("👥 Список студентов:\n\n");
        for (StudentsResponseDto student : students) {
            sb.append("👤 ").append(student.getFullName()).append("\n")
                    .append("🔢 ISU: ").append(student.getIsuNumber()).append("\n")
                    .append("🎓 Группа: ").append(student.getStudyGroup().getNumber()).append("\n")
                    .append("---------------------------\n");
        }
        return sb.toString();
    }

    public record GetStudentsFilters(String groupNumber, Boolean isStatementDelivered, Boolean isStatementSigned,
                                     Boolean isStatementScanned, Boolean isNotificationSent) {

    }
}
