package org.example.studentdistributionbot.client.apply_controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.dto.ApplyResponseDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetApplyClient {
    private final WebClient webClient;

    public List<ApplyResponseDto> getApplies(String status, String groupName, String isuNumber) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/apply")
                        .queryParamIfPresent("status", Optional.ofNullable(status))
                        .queryParamIfPresent("groupName", Optional.ofNullable(groupName))
                        .queryParamIfPresent("isuNumber", Optional.ofNullable(isuNumber))
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ApplyResponseDto>>() {
                })
                .block();
    }
}
