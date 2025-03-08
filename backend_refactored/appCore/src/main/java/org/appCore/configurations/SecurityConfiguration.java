package org.appCore.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("This function was called");
        
        http.httpBasic(Customizer.withDefaults())
               .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

                // .authorizeHttpRequests(auth -> auth
                //                 .requestMatchers(RegexRequestMatcher.regexMatcher("/api/auth/register/[A-Za-z]+")).permitAll()
//                            .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.DELETE,"/api/company/[A-Za-z0-9/]+")).hasAuthority(AuthoritiesManager.CAN_UPDATE_COMPANY_DETAILS_STR)
//                            .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.POST, "/api/company/[A-Za-z0-9/]+")).hasAuthority(AuthoritiesManager.CAN_UPDATE_COMPANY_DETAILS_STR)
//                            .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.PUT, "/api/company/[A-Za-z0-9/]+")).hasAuthority(AuthoritiesManager.CAN_UPDATE_COMPANY_DETAILS_STR)
//                            .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.GET, "/api/company/[A-Za-z0-9/]+")).hasAuthority(AuthoritiesManager.CAN_VIEW_COMPANY_DETAILS_STR)
                // );

        return http.build();
    }
}