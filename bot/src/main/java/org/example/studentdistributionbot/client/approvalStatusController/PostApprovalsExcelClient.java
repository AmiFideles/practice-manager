package org.example.studentdistributionbot.client.approvalStatusController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostApprovalsExcelClient {
    private final WebClient webClient;

    public HttpStatusCode postApprovalsExcel(String fileName, InputStream stream) {
        try {
            InputStreamResource resource = new InputStreamResource(stream) {
                @Override
                public String getFilename() {
                    return fileName;
                }

                @Override
                public long contentLength() {
                    return -1;
                }
            };
            var statusCode = webClient.post()
                    .uri("/api/aprovals")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData("file", resource))
                    .exchangeToMono(response -> Mono.just(response.statusCode()))
                    .block();

            log.info("Ответ от сервиса: {}", statusCode);
            return statusCode;
        } catch (Exception e) {
            log.error("Ошибка при отправке файла на /students/upload, message : {}", e.getMessage());
            return null;
        }
    }
}
