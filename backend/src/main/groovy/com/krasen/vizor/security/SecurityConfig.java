package com.krasen.vizor.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true, prePostEnabled = false)
public class SecurityConfig {
    private final JwtService jwt;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtService jwt, CorsConfigurationSource corsConfigurationSource) {
        this.jwt = jwt;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_FORBIDDEN))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/campaigns").permitAll()
                        .requestMatchers(HttpMethod.GET, "/campaigns/{id}").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthFilter(jwt), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    static class JwtAuthFilter extends OncePerRequestFilter {
        private final JwtService jwt;

        JwtAuthFilter(JwtService jwt) {
            this.jwt = jwt;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest req,
                                        HttpServletResponse res,
                                        FilterChain chain)
                throws ServletException, IOException {

            String header = req.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                try {
                    var jws = jwt.parse(token);
                    var email = jws.getPayload().getSubject();
                    var roles = jws.getPayload().get("roles", List.class);

                    var authorities = new ArrayList<GrantedAuthority>();
                    if (roles != null && !roles.isEmpty()) {
                        for (Object role : roles) {
                            if (role != null) {
                                String roleName = role.toString().toUpperCase();
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
                            }
                        }
                    }

                    // Only set authentication if we have valid authorities
                    // This allows public endpoints to work even without roles
                    if (!authorities.isEmpty()) {
                        var auth = new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                authorities
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (Exception e) {
                    System.err.println("JWT parsing error: " + e.getMessage());
                }
            }

            chain.doFilter(req, res);
        }
    }
}

