package org.example.studentdistributionbot.client.apply_controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.dto.PracticeApplicationRequest;
import org.example.studentdistributionbot.dto.RegisterDto;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostApplyClient {

    private final WebClient webClient;
    public String postApply(PracticeApplicationRequest practiceApplicationRequest) {
        try {
            return webClient.post()
                    .uri("/api/apply")
                    .bodyValue(practiceApplicationRequest)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse.bodyToMono(JsonNode.class)
                            .map(json -> new RuntimeException(json.path("message").asText())))
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Ошибка при заполнение заявки, неправильный формат. {}", e.getMessage());
            return e.getMessage();
        }
    }
}
