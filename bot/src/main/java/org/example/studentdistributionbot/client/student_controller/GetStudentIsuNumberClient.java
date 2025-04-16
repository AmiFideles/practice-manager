package org.example.studentdistributionbot.client.student_controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetStudentIsuNumberClient {
    private final WebClient webClient;

    public String getStudentIsuNumber(String isuNumber) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/students/" + isuNumber)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Ошибка при запросе: {}", e.getMessage());
            return "Студент с id не найден";
        }
    }
}
