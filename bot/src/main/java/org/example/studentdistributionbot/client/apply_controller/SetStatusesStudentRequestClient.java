package org.example.studentdistributionbot.client.apply_controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.dto.ApplyStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class SetStatusesStudentRequestClient {

    private final WebClient webClient;

    public String setStatus(ApplyStatus applyStatus, String isuNumber) {
        try {
            webClient.put()
                    .uri(uriBuilder -> uriBuilder.path("api/apply/{isuNumber}")
                            .queryParam("status", applyStatus.toString())
                            .build(isuNumber))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse.bodyToMono(JsonNode.class)
                            .map(json -> new RuntimeException(json.path("message").asText())))
                    .bodyToMono(Void.class)
                    .block();
            return "Статус успешно обновлен";
        } catch (Exception e) {
            log.error("Ошибка при запросе в api/apply/{isuNumber} с isuNumber: {}", isuNumber);
            return e.getMessage();
        }
    }
}
