package by.ares.apigateway.filter;

import by.ares.apigateway.dto.request.AccessTokenRequest;
import by.ares.apigateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static by.ares.apigateway.util.ApiGatewayConstants.URI_VALIDATE_POSTFIX;

@Component
@RequiredArgsConstructor
public class AuthFilter extends AbstractGatewayFilterFactory<Object> {

    private final WebClient webClient;
    private final JwtUtil jwtUtil;
    @Value("${AUTH_SERVICE_URL:}")
    private String authServiceUri;

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) ->
                Mono.fromCallable(() -> jwtUtil.extractToken(exchange)).flatMap(token -> webClient.post()
                        .uri(authServiceUri + URI_VALIDATE_POSTFIX)
                        .bodyValue(new AccessTokenRequest(token))
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .flatMap(isValid -> {
                            if (!isValid) {
                                return unauthorizedResponse(exchange, "Token validation failed");
                            }
                            var mutatedExchange = setHeaders(exchange, token);
                            return chain.filter(mutatedExchange);
                        })
                        .onErrorResume(t ->
                                unauthorizedResponse(exchange, "Token validation failed: " + t.getMessage())));
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String exceptionResponse) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(exceptionResponse.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private ServerWebExchange setHeaders(ServerWebExchange exchange, String token) {
        String userId = String.valueOf(jwtUtil.extractId(token));
        String role = String.valueOf(jwtUtil.extractRole(token));
        return exchange.mutate()
                .request(exchange.getRequest()
                        .mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Role", role)
                        .build())
                .build();
    }

}
