package org.example.studentdistributionbot;

import org.example.studentdistributionbot.dto.RegisterDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserContextStorage {
    private final Map<Long, BotState> states = new HashMap<>();
    private final Map<Long, RegisterDto> data = new HashMap<>();

    public BotState getState(Long chatId) {
        return states.getOrDefault(chatId, BotState.IDLE);
    }

    public RegisterDto getData(Long chatId) {
        return data.computeIfAbsent(chatId, id -> new RegisterDto());
    }

    public void setState(Long chatId, BotState state) {
        states.put(chatId, state);
    }

    public void clear(Long chatId) {
        states.remove(chatId);
        data.remove(chatId);
    }
}
