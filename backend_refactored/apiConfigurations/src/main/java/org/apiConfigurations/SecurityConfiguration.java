package org.apiConfigurations;

import org.access.AuthoritiesManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("This function was called");
        
        http.httpBasic(Customizer.withDefaults())
               .csrf(AbstractHttpConfigurer::disable)
                // any request to the api/auth/register is allowed
                .authorizeHttpRequests(auth -> auth.requestMatchers(RegexRequestMatcher.regexMatcher("/api/auth/register/[A-Za-z\\/]+")).permitAll()
                .requestMatchers(RegexRequestMatcher.regexMatcher("/api/url/encode")).hasAuthority(AuthoritiesManager.CAN_ENCODE_URL_STR)
                .requestMatchers(RegexRequestMatcher.regexMatcher("/api/url/decode")).hasAuthority(AuthoritiesManager.CAN_USE_SHORT_URL_STR)
                .requestMatchers(RegexRequestMatcher.regexMatcher("/api/url/history")).hasAuthority(AuthoritiesManager.CAN_VIEW_HISTORY_STR)
                .anyRequest().authenticated()                
                );

        return http.build();
    }
}