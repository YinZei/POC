package com.example.poc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@Slf4j
public class SecurityConfig {
    private final HttpStatusEntryPoint httpStatusEntryPoint = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    private final AccessDeniedHandler accessDeniedHandler = (request, response, accessDeniedException) -> response.setStatus(HttpStatus.UNAUTHORIZED.value());

    public SecurityConfig() {
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            POCUser tacoUser = userRepository.findByUsername(username);
            if (tacoUser != null) return tacoUser;

            throw new UsernameNotFoundException("User '" + username + "' not found");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(i -> i.requestMatchers(HttpMethod.PUT, "/user/*").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/user/*").authenticated().
                requestMatchers("**").permitAll()
        )
                .logout(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable).httpBasic(i->i.authenticationEntryPoint(httpStatusEntryPoint)).requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)
                .exceptionHandling(i->i.authenticationEntryPoint(httpStatusEntryPoint).accessDeniedHandler(accessDeniedHandler))
                .anonymous(AbstractHttpConfigurer::disable)
                .headers(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain oauthFilterChain(HttpSecurity http) throws Exception {
        return http.securityMatcher("/welcome").authorizeHttpRequests(i->i.anyRequest().authenticated()).oauth2Login(Customizer.withDefaults()).build();
    }

    @Bean
    ApplicationRunner addAdmin(UserRepository userRepository, PasswordEncoder encoder) {
        return i -> {
            POCUser tmp = new POCUser();
            tmp.setUsername("admin");
            tmp.setPassword(encoder.encode("123456"));
            log.info(userRepository.save(tmp).toString());
        };
    }
}
