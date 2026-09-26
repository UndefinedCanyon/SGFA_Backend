package com.sgfa.backend.infrastructure.config;

import com.sgfa.backend.infrastructure.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/artesanos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ferias").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/edicionesferia").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/lugares").permitAll()

                        // Rutas exclusivas de administrador
                        .requestMatchers(HttpMethod.POST, "/api/ferias").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/edicionesferia").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/lugares").hasRole("ADMINISTRADOR")

                        // Cualquier otra ruta no listada explícitamente, requiere estar autenticado
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}