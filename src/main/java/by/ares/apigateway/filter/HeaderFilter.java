package by.ares.apigateway.filter;

import by.ares.apigateway.config.GatewayFilterConfig;
import by.ares.apigateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class HeaderFilter extends AbstractGatewayFilterFactory<GatewayFilterConfig> {

    private final JwtUtil jwtUtil;

    @Override
    public GatewayFilter apply(GatewayFilterConfig config) {
        return (exchange, chain) -> {
            String token = jwtUtil.extractToken(exchange);
            HttpHeaders headers = exchange.getRequest().getHeaders();
            headers.add("X-User-Id", String.valueOf(jwtUtil.extractId(token)));
            headers.add("X-User-Role", String.valueOf(jwtUtil.extractRole(token)));
            return chain.filter(exchange);
        };
    }

}
