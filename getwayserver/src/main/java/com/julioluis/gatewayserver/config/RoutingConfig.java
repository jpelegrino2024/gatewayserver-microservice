package com.julioluis.gatewayserver.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class RoutingConfig {

    @Bean
    public RouteLocator wireMoneyConfig(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(p ->
                        p.path("/wiremoney/bank/**")
                                .filters(f -> f.rewritePath("/wiremoney/bank/(?<segment>.*)",
                                        "/${segment}")
                                        .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                )
                                .uri("lb://BANK"))
                .route(p ->
                        p.path("/wiremoney/customer/**")
                                .filters(f -> f.rewritePath("/wiremoney/customer/(?<segment>.*)",
                                        "/${segment}")
                                        .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                )
                                .uri("lb://CUSTOMER"))

                .route(p ->
                        p.path("/wiremoney/wiretransfer/**")
                                .filters(f -> f.rewritePath("/wiremoney/wiretransfer/(?<segment>.*)",
                                        "/${segment}")
                                        .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                )
                                .uri("lb://WIRETRANSFER"))
                .build();


    }
}
