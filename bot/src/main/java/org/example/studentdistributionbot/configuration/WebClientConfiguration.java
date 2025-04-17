package org.example.studentdistributionbot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient webClient() {
        HttpClient client = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(30));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://backend:8080")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
