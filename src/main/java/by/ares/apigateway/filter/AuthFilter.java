package by.ares.apigateway.filter;

import by.ares.apigateway.config.GatewayFilterConfig;
import by.ares.apigateway.dto.request.AccessTokenRequest;
import by.ares.apigateway.exception.AccessDeniedException;
import by.ares.apigateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthFilter extends AbstractGatewayFilterFactory<GatewayFilterConfig> {

    private final WebClient webClient;
    private final JwtUtil jwtUtil;

    @Override
    public GatewayFilter apply(GatewayFilterConfig gatewayFilterConfig) {
        return (exchange, chain) -> {
            String token = jwtUtil.extractToken(exchange);
            return webClient.post()
                    .bodyValue(new AccessTokenRequest(token))
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .flatMap(isValid -> {
                        if (isValid) {
                            return chain.filter(exchange);
                        }
                        throw new RuntimeException("");
                    })
                    .onErrorResume(t -> Mono.error(new AccessDeniedException("Auth service error: " + t.getMessage())));
        };
    }

}
