package org.example.studentdistributionbot.client.approvalStatusController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.dto.ApprovalStatusDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetApprovalsStatusClient {

    private final WebClient webClient;

    public String getApprovalsStatus() {
        try {
            return webClient.get()
                    .uri("/api/approvals/status")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Ошибка при регистрации: {}", e.getMessage());
            return null;
        }
    }
}
