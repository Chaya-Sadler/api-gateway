package com.chayasadler.api_gateway.config;

import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

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
    public RouterFunction<ServerResponse> routeAuthService() {

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
    public RouterFunction<ServerResponse> routeProtectedProductService() {

        return GatewayRouterFunctions
                .route("product-service-route")
                .route(path("/app/products").and(GET("/app/products")),HandlerFunctions.http())
                .filter(authenticationFilter)
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("productServiceCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .before(BeforeFilterFunctions.uri("http://localhost:8083"))
                .build();

    }

    @Bean
    public RouterFunction<ServerResponse> routeProtectedOrderService() {
        return GatewayRouterFunctions
                .route("order-service-route")
                .route(path("/app/orders").and(POST("/app/orders")),HandlerFunctions.http())
                .filter(authenticationFilter)
                .before(BeforeFilterFunctions.uri("http://localhost:8084"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> fallBackRoute() {
        return GatewayRouterFunctions
                .route("fallback-route")
                .GET("/fallbackRoute", request ->
                        ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body("Service unavailable. Please try again later"))
                .build();
    }
}
