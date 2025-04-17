package org.example.studentdistributionbot.client.apply_controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.dto.PracticeApplicationRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostApplyClient {

    private final WebClient webClient;

    public String postApply(PracticeApplicationRequest practiceApplicationRequest) {
        try {
            var status = webClient.post()
                    .uri("/api/apply")
                    .bodyValue(practiceApplicationRequest)
                    .exchangeToMono(response -> {
                        if (response.statusCode().is2xxSuccessful()) {
                            return Mono.just(response.statusCode());
                        } else {
                            return response.bodyToMono(JsonNode.class)
                                    .flatMap(json -> {
                                        String errorMessage = json.path("message").asText("Ошибка без сообщения");
                                        log.error("Ошибка отправки: {}", errorMessage);
                                        return Mono.just(response.statusCode());
                                    });
                        }
                    })
                    .block();
            if (status != null && status.is2xxSuccessful()) {
                return "Успешно";
            } else {
                return "Ошибка";
            }
        } catch (Exception e) {
            log.error("Ошибка при заполнение заявки, неправильный формат. {}", e.getMessage());
            return e.getMessage();
        }
    }
}
