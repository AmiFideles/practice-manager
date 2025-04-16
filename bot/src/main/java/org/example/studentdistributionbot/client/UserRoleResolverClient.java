package org.example.studentdistributionbot.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.dto.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleResolverClient {
    private final WebClient webClient;

    public UserRole getUserRole(Long telegramId) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/user/role")
                            .queryParam("telegramId", telegramId)
                            .build())
                    .retrieve()
                    .bodyToMono(UserRole.class)
                    .block();
        } catch (Exception e) {
            log.error("User role exception: {}", e.getMessage());
            return new UserRole("UNKNOWN");
        }
    }
}
