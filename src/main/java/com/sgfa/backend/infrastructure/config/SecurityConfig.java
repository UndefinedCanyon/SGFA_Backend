package com.sgfa.backend.infrastructure.config;

import com.sgfa.backend.infrastructure.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final String origenesPermitidos;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          @Value("${CORS_ORIGINS:http://localhost:5173}") String origenesPermitidos) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.origenesPermitidos = origenesPermitidos;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Usuarios vacíos a propósito: la autenticación es solo por JWT. Definir este bean evita que
    // Spring cree un usuario de prueba con una contraseña generada que se imprime en el log.
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origenes = Arrays.stream(origenesPermitidos.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toList();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenes);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Documentación y página de error
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Rutas públicas
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/artesanos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ferias").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/edicionesferia").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/edicionesferia/*/participantes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/edicionesferia/*/ocupacion").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/lugares").permitAll()

                        // Solo el administrador
                        .requestMatchers(HttpMethod.POST, "/api/ferias").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/ferias/todas").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/ferias/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/edicionesferia").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/edicionesferia/todas").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/edicionesferia/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/lugares").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/artesanos").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/inscripciones").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/inscripciones/**").hasRole("ADMINISTRADOR")

                        // Solo el artesano (el orden importa: el perfil va antes de PUT /api/artesanos/**)
                        .requestMatchers(HttpMethod.GET, "/api/artesanos/perfil").hasRole("ARTESANO")
                        .requestMatchers(HttpMethod.PUT, "/api/artesanos/perfil").hasRole("ARTESANO")
                        .requestMatchers(HttpMethod.PUT, "/api/artesanos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/productos").hasRole("ARTESANO")
                        .requestMatchers(HttpMethod.GET, "/api/productos").hasRole("ARTESANO")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasRole("ARTESANO")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ARTESANO")
                        .requestMatchers(HttpMethod.POST, "/api/inscripciones").hasRole("ARTESANO")
                        .requestMatchers(HttpMethod.GET, "/api/inscripciones/mias").hasRole("ARTESANO")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}