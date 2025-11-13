package com.chayasadler.api_gateway.filter;

import com.chayasadler.api_gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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
    public HandlerFilterFunction<ServerResponse, ServerResponse> authenticationFilter(){
        return (request, next) -> {
            // Pre-processing logic for ServerRequest
            String authHeader = request.headers().firstHeader("Authorization");
            if(authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length());

                if(!jwtUtil.validateToken(token)){
                    System.out.println(" Token invalid");
                }
            }
            return next.handle(request);
        };
    }


}
