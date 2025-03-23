package org.apiUtils.configurations;

import org.access.AuthoritiesManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
@EnableWebSecurity
@SuppressWarnings("unused")
public class SecurityConfiguration {
    @SuppressWarnings("Convert2MethodRef")
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("This function was called");
        
        http.httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                // any request to the api/auth/register is allowed
                .authorizeHttpRequests(
                    auth -> auth.requestMatchers(RegexRequestMatcher.regexMatcher("/api/auth/register/[A-Za-z\\/]+")).permitAll()
                    // Allow Swagger UI resources without authentication
                    .requestMatchers("/swagger-ui.html").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/api-docs/**").permitAll()
                    .requestMatchers("/swagger-resources/**").permitAll()
                    .requestMatchers("/webjars/**").permitAll()
                    
                    // url encoding endpoints
                    .requestMatchers(RegexRequestMatcher.regexMatcher("/api/url/encode")).hasAuthority(AuthoritiesManager.CAN_ENCODE_URL_STR)
                    .requestMatchers(RegexRequestMatcher.regexMatcher("/api/url/decode")).hasAuthority(AuthoritiesManager.CAN_USE_SHORT_URL_STR)
                    .requestMatchers(RegexRequestMatcher.regexMatcher("/api/url/history")).hasAuthority(AuthoritiesManager.CAN_VIEW_HISTORY_STR)

                    // 
                    .anyRequest().authenticated()                
                );

        return http.build();
    }
    
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**");
    }
}