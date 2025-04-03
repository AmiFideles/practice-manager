package ru.itmo.practicemanager.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Practice-manager API",
        version = "v1",
        description = "API для распределения студентов по практикам"
))
public class OpenApiConfig {
}
