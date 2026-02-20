package com.example.studentmanagement.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Static resources
                .requestMatchers("/", "/index.html", "/static/**", "/*.html", "/*.js", "/*.css").permitAll()

                // Public endpoints - anyone can register
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/departments/**").permitAll()

                // Course endpoints - Only TEACHER can create, update, delete courses
                .requestMatchers(HttpMethod.POST, "/api/courses/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.PUT, "/api/courses/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.GET, "/api/courses/**").authenticated()

                // Department management - Only TEACHER can create, update, delete departments
                .requestMatchers(HttpMethod.POST, "/api/departments/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.PUT, "/api/departments/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.DELETE, "/api/departments/**").hasRole("TEACHER")

                // Student delete - Only TEACHER can delete students (single segment only)
                .requestMatchers(HttpMethod.DELETE, "/api/students/{id}").hasRole("TEACHER")

                // All other student endpoints
                .requestMatchers("/api/students/**").authenticated()

                // Teacher endpoints
                .requestMatchers("/api/teachers/**").authenticated()

                // Any other request needs authentication
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
