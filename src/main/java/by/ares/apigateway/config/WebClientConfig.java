package by.ares.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder,
            @Value("${auth.service.url") String authValidateUrl) {
        return webClientBuilder.baseUrl(authValidateUrl).build();
    }

}
