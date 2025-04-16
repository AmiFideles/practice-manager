package org.example.studentdistributionbot.commands;

import org.example.studentdistributionbot.BotState;
import org.example.studentdistributionbot.Command;
import org.example.studentdistributionbot.client.UserRoleResolverClient;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class HelpCommandHandler implements BotCommandHandler {

    private final UserRoleResolverClient userRoleResolverClient;
    private final Command[] allCommands;

    public HelpCommandHandler(UserRoleResolverClient userRoleResolverClient) {
        this.userRoleResolverClient = userRoleResolverClient;
        allCommands = Command.values();
    }


    @Override
    public Command getCommand() {
        return Command.HELP;
    }

    @Override
    public void handleCommand(Update update, TelegramClient client) throws TelegramApiException {
        var userRole = userRoleResolverClient.getUserRole(update.getMessage().getFrom().getId());
        StringBuilder message = new StringBuilder("Список доступных команд:  \n");
        if (userRole.getRole().equals("ADMIN")) {
            for (Command command : allCommands) {
                message.append("/").append(command.getValue()).append(": ").append(command.getDescription()).append("\n");
            }
        } else {
            for (Command command : allCommands) {
                if (!command.getIsAdminCommand()) {
                    message.append("/").append(command.getValue()).append(": ").append(command.getDescription()).append("\n");
                }
            }
        }
        Long chatId = update.getMessage().getChatId();
        sendMessage(chatId, client, message.toString());
    }
}

