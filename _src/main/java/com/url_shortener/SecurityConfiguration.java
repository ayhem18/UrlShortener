package com.url_shortener;

import com.url_shortener.Service.AuthoritiesManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                            .requestMatchers(RegexRequestMatcher.regexMatcher("/api/auth/register/[A-Za-z]+")).permitAll()
                            .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.DELETE,"/api/company/[A-Za-z0-9/]+")).hasAuthority(AuthoritiesManager.CAN_UPDATE_COMPANY_DETAILS)
                            .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.POST, "/api/company/[A-Za-z0-9/]+")).hasAuthority(AuthoritiesManager.CAN_UPDATE_COMPANY_DETAILS)
                            .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.PUT, "/api/company/[A-Za-z0-9/]+")).hasAuthority(AuthoritiesManager.CAN_UPDATE_COMPANY_DETAILS)
                            .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.GET, "/api/company/[A-Za-z0-9/]+")).hasAuthority(AuthoritiesManager.CAN_VIEW_COMPANY_DETAILS)
                );

        return http.build();
    }
}