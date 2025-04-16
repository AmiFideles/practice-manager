package org.example.studentdistributionbot.client.apply_controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetApplyPdfClient {

    private final WebClient webClient;

    public byte[] getPdfForTelegramId(Long telegramId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("api/apply/pdf")
                        .queryParam("telegramId", telegramId)
                        .build())
                .accept(MediaType.APPLICATION_PDF)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }
}
