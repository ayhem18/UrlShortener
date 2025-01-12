package com.url_shortener;

import com.url_shortener.Service.AuthoritiesManager;
import com.url_shortener.Service.RoleManager;
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
//                            .requestMatchers(HttpMethod.POST, "api/auth/register/company").permitAll()
//                            .requestMatchers(HttpMethod.POST, "api/auth/register/user").permitAll()
                            .requestMatchers(RegexRequestMatcher.regexMatcher("/api/auth/register/[A-Za-z]+")).permitAll()
                            .requestMatchers(RegexRequestMatcher.regexMatcher("/api/company/[A-Za-z]+")).hasAuthority(AuthoritiesManager.CAN_UPDATE_COMPANY) // any api/company endpoint
                );

//                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // for the moment, let everyone have access to the endpoints

        return http.build();
    }
}