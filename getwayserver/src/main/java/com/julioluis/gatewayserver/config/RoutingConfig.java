package com.julioluis.gatewayserver.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;
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
                                        .retry(retryConfig -> retryConfig.setRetries(3)
                                                .setMethods(HttpMethod.GET)
                                                .setBackoff(Duration.ofMillis(100),Duration.ofMillis(1000),2,true))

                                )
                                .uri("lb://BANK"))
                .route(p ->
                        p.path("/wiremoney/customer/**")
                                .filters(f -> f.rewritePath("/wiremoney/customer/(?<segment>.*)",
                                        "/${segment}")
                                        .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                        .circuitBreaker(config -> config.setName("customerCircuitBreaker")
                                                .setFallbackUri("forward:/contactSupport"))
                                )
                                .uri("lb://CUSTOMER"))

                .route(p ->
                        p.path("/wiremoney/wiretransfer/**")
                                .filters(f -> f.rewritePath("/wiremoney/wiretransfer/(?<segment>.*)",
                                        "/${segment}")
                                        .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                        .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter())
                                                .setKeyResolver(userKeyResolver()))
                                )
                                .uri("lb://WIRETRANSFER"))
                .build();


    }
@Bean
public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(1,1,1);
}

@Bean
public KeyResolver userKeyResolver() {
        return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("user"))
                .defaultIfEmpty("anonymous");
}

}
