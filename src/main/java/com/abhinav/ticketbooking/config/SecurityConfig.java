package com.abhinav.ticketbooking.config;

import com.abhinav.ticketbooking.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints: Auth and Webhooks
                        .requestMatchers("/api/auth", "/api/auth/**").permitAll()
                        .requestMatchers("/api/payments/webhook").permitAll()

                        // Public browsing endpoints (GET)
                        .requestMatchers(HttpMethod.GET, "/api/movies", "/api/movies/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/theatres", "/api/theatres/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/screens", "/api/screens/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/seats", "/api/seats/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/shows", "/api/shows/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/show-seats", "/api/show-seats/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tickets", "/api/tickets/**").permitAll()

                        // Admin-only management endpoints
                        .requestMatchers(HttpMethod.POST, "/api/movies", "/api/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/movies", "/api/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/movies", "/api/movies/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/theatres", "/api/theatres/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/theatres", "/api/theatres/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/theatres", "/api/theatres/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/screens", "/api/screens/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/screens", "/api/screens/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/screens", "/api/screens/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/seats", "/api/seats/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/seats", "/api/seats/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/shows", "/api/shows/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/shows", "/api/shows/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/shows", "/api/shows/**").hasRole("ADMIN")

                        .requestMatchers("/api/show-seats/initialize", "/api/show-seats/initialize/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/tickets/validate", "/api/tickets/validate/**").hasRole("ADMIN")

                        // Actuator / Error endpoints
                        .requestMatchers("/error", "/actuator/**").permitAll()

                        // All other endpoints require authentication (booking, payments, etc.)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                frontendUrl,
                "http://localhost:5173",
                "http://localhost:3000",
                "http://127.0.0.1:5173"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
