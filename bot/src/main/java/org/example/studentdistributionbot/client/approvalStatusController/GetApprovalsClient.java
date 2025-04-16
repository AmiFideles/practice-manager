package org.example.studentdistributionbot.client.approvalStatusController;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.dto.GetApprovalsDto;
import org.example.studentdistributionbot.dto.GroupResponseDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetApprovalsClient {

    private final WebClient webClient;

    public List<GroupResponseDto> getApprovals(GetApprovalsDto getApprovalsDto) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/approvals")
                            .queryParamIfPresent("studyGroupName", Optional.ofNullable(getApprovalsDto.getStudyGroupName()))
                            .queryParamIfPresent("status", Optional.ofNullable(getApprovalsDto.getStatus()))
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<GroupResponseDto>>() {
                    })
                    .block();
        } catch (Exception e) {
            log.error("Ошибка при регистрации: {}", e.getMessage());
            return null;
        }
    }
}
