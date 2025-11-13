package com.chayasadler.api_gateway.config;

import com.chayasadler.api_gateway.filter.JwtAuthenticationFilter;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;
import static org.springframework.web.servlet.function.RequestPredicates.GET;
import static org.springframework.web.servlet.function.RequestPredicates.POST;

@Configuration
public class GatewayConfig {

    HandlerFilterFunction<ServerResponse, ServerResponse> authenticationFilter;

    public GatewayConfig(HandlerFilterFunction<ServerResponse, ServerResponse> authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public RouterFunction<ServerResponse> routeAuthService(){

        return GatewayRouterFunctions.route("auth-service-route")
                .route(path("/auth/register")
                        .and(POST("/auth/register")),
                        HandlerFunctions.http())
                .before(BeforeFilterFunctions.uri("http://localhost:8081"))

                .route(path("/auth/login")
                        .and(POST("/auth/login")),
                        HandlerFunctions.http())
                .before(BeforeFilterFunctions.uri("http://localhost:8081"))

                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> routeProtectedService(){

        return GatewayRouterFunctions.route("product-service-route")
                .route(path("/product/products")
                .and(GET("/product/products")),
                        HandlerFunctions.http())
                .filter(authenticationFilter)
                .before(BeforeFilterFunctions.uri("http://localhost:8083"))
                .build();

    }
}
