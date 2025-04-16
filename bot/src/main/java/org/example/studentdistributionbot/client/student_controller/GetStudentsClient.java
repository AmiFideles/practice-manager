package org.example.studentdistributionbot.client.student_controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentdistributionbot.commands.student_controller.GetStudentsCommandHandler;
import org.example.studentdistributionbot.dto.StudentsResponseDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetStudentsClient {

    private final WebClient webClient;

    public List<StudentsResponseDto> getStudents(GetStudentsCommandHandler.GetStudentsFilters filters) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/students")
                        .queryParamIfPresent("groupNumber", Optional.ofNullable(filters.groupNumber()))
                        .queryParamIfPresent("isStatementDelivered", Optional.ofNullable(filters.isStatementDelivered()))
                        .queryParamIfPresent("isStatementSigned", Optional.ofNullable(filters.isStatementSigned()))
                        .queryParamIfPresent("isStatementScanned", Optional.ofNullable(filters.isStatementScanned()))
                        .queryParamIfPresent("isNotificationSent", Optional.ofNullable(filters.isNotificationSent()))
                        .build()
                )
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<StudentsResponseDto>>() {
                })
                .block();
    }
}
