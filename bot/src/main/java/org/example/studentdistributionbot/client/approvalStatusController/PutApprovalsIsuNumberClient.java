package org.example.studentdistributionbot.client.approvalStatusController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.dto.ApprovalStatusDTO;
import org.example.studentdistributionbot.dto.GetApprovalsDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PutApprovalsIsuNumberClient {

    private final WebClient webClient;
    public String putApprovals(ApprovalStatusDTO approvalStatusDTO, String isuNumber) {
        try {
            return webClient.put()
                    .uri("/api/approvals/" + isuNumber)
                    .bodyValue(approvalStatusDTO)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Ошибка при регистрации: {}", e.getMessage());
            return null;
        }
    }

}
