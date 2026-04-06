package by.ares.apigateway.util;

import by.ares.apigateway.exception.AccessDeniedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static by.ares.apigateway.util.ApiGatewayConstants.CLAIM_NAME_ROLE;
import static by.ares.apigateway.util.ApiGatewayConstants.CLAIM_NAME_USER_ID;

@Component
public class JwtUtil {

    @Value("${JWT_SECRET_KEY}")
    private String secret;


    public String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AccessDeniedException( "Missing or invalid Authorization header");
        }
        return authHeader.substring(7);
    }

    public Long extractId(String token) {
        return extractClaims(token).get(CLAIM_NAME_USER_ID, Long.class);
    }

    public Role extractRole(String token) {
        return Role.valueOf(extractClaims(token).get(CLAIM_NAME_ROLE, String.class));
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

}
