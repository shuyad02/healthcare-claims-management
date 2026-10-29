package com.healthcare.claims.config;

import com.healthcare.claims.security.CustomUserDetailsService;
import com.healthcare.claims.security.JwtAuthenticationFilter;
import com.healthcare.claims.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider tokenProvider;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtTokenProvider tokenProvider) {
        this.userDetailsService = userDetailsService;
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, userDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configure(http))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/plans/**").permitAll()
                // Admin endpoints
                .requestMatchers(HttpMethod.POST, "/api/employees/**").hasAnyRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/employees/**").hasAnyRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasAnyRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/plans/**").hasAnyRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/plans/**").hasAnyRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/plans/**").hasAnyRole("ADMIN")
                // HR / Claims Officer & Admin claim review/approve/reject endpoints
                .requestMatchers(HttpMethod.PATCH, "/api/claims/*/review").hasAnyRole("HR_OFFICER", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/claims/*/approve").hasAnyRole("HR_OFFICER", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/claims/*/reject").hasAnyRole("HR_OFFICER", "ADMIN")
                .anyRequest().authenticated()
            );

        // Frame options for H2 console if used
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
