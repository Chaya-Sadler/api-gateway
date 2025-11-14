package com.chayasadler.api_gateway.filter;

import com.chayasadler.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Locale;

@Component
public class JwtAuthenticationFilter {

    @Autowired
    JwtUtil jwtUtil;

    private static final String BEARER_PREFIX = "Bearer ";

    @Bean
    public HandlerFilterFunction<ServerResponse, ServerResponse> authenticationFilter() {

        return (request, next) -> {

            String authHeader = request.headers().firstHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                return ServerResponse.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization Header!");
            }
            String token = authHeader.substring(BEARER_PREFIX.length());

            if (!jwtUtil.validateToken(token)) {
                return ServerResponse.status(HttpStatus.UNAUTHORIZED).body(" Invalid or Expired Token");
            }
            String customerId = jwtUtil.getCustomerId();
            ServerRequest serverRequest = ServerRequest.from(request)
                    .header("X-Customer-Id", customerId)
                    .build();

            return next.handle(serverRequest);
        };
    }
}
