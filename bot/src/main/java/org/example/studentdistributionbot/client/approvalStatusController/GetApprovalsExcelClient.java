package org.example.studentdistributionbot.client.approvalStatusController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.dto.GetApprovalsDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetApprovalsExcelClient {
    private final WebClient webClient;

    public byte[] getApprovalsExcel() {
        try {
            return webClient.get()
                    .uri("/api/approvals/excel")
                    .accept(MediaType.APPLICATION_OCTET_STREAM)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
        } catch (Exception e) {
            log.error("Ошибка при регистрации: {}", e.getMessage());
            return null;
        }
    }
}
