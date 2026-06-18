package com.moving.reservation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI moveMissionOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Move Mission Control API")
                        .version("v1")
                        .description("이사 예약 고객 API와 운영 API 문서입니다."))
                .servers(List.of(new Server()
                        .url("http://localhost:8081")
                        .description("Local development server")));
    }
}
