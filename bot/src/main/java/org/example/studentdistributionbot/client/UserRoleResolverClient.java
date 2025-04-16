package org.example.studentdistributionbot.client;

import lombok.RequiredArgsConstructor;
import org.example.studentdistributionbot.dto.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class UserRoleResolverClient {
    private final WebClient webClient;

    public UserRole getUserRole(Long telegramId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/user/role")
                        .queryParam("telegramId", telegramId)
                        .build())
                .retrieve()
                .bodyToMono(UserRole.class)
                .block();

    }
}
