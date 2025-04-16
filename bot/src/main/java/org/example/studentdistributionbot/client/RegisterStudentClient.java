package org.example.studentdistributionbot.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.dto.RegisterDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterStudentClient {

    private final WebClient webClient;

    public String registerUser(RegisterDto registerDto) {
        try {
            return webClient.post()
                    .uri("/api/user/register")
                    .bodyValue(registerDto)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Ошибка при регистрации: {}", e.getMessage());
            return null;
        }
    }


}
