package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 根据需要决定是否禁用 CSRF
                .cors(cors -> cors.configurationSource(request -> {
                    var source = new org.springframework.web.cors.CorsConfiguration();
                    source.setAllowedOrigins(List.of("http://localhost:8080")); // Vue 应用的端口
                    source.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    source.setAllowedHeaders(List.of("*"));
                    source.setAllowCredentials(true);
                    return source;
                }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/user/**").permitAll()
                        .requestMatchers("/api/categories/**").permitAll()
                        .requestMatchers("/api/artworkImages/**").permitAll()
                        .requestMatchers("/api/artworks/**").permitAll()
                        .requestMatchers("/api/auctions/**").permitAll()
                        .requestMatchers("/api/auctionRegistrations/**").permitAll()
                        .requestMatchers("/api/bids/**").permitAll()
                        .requestMatchers("/api/orders/**").permitAll()
                        .requestMatchers("/api/shipping/**").permitAll()
                        .requestMatchers("/api/carousel/**").permitAll()
                        .requestMatchers("/api/favorites/**").permitAll()
                        .requestMatchers("/api/reviews/**").permitAll()
                        .requestMatchers("/api/user/checkUsername").permitAll()
                        .requestMatchers("/api/user/getUserByRole").permitAll()
                        .requestMatchers("/api/user/{id}/uploadAvatar").permitAll()
                        .requestMatchers("/api/user/login").permitAll()
                        .requestMatchers("/api/user/add").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
