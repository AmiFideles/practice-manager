package org.example.studentdistributionbot.client.student_controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentsReportGetClient {

    private final WebClient webClient;

    public byte[] getExcelReport() {
        return webClient.get()
                .uri("api/students/report")
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }
}
