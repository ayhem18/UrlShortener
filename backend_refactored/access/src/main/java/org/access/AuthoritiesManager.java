package org.access;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthoritiesManager {
    private static final Map<String, GrantedAuthority> AUTHORITIES = Map.of(
            "CAN_UPDATE_DOMAIN_NAME", new CanUpdateDomainName(),
            "CAN_DELETE_ACCESS", new CanDeleteAccess(),
            "CAN_CANCEL_SUBSCRIPTION", new CanCancelSubscription(),
            "CAN_VIEW_COMPANY_DETAILS", new CanViewCompanyDetails(),
            "CAN_VIEW_SUBSCRIPTION", new CanViewSubscription(),
            "CAN_GENERATE_TOKENS", new CanGenerateTokens(),
            "CAN_USE_SHORT_URL", new CanUseShortUrl(),
            "CAN_ENCODE_URL", new CanEncodeUrl()
    );

    public static List<GrantedAuthority> getAuthorities(List<String> authorities) {
        return authorities.stream()
                .map(AUTHORITIES::get)
                .collect(Collectors.toList());
    }

    public static boolean hasAuthority(List<? extends GrantedAuthority> userAuthorities, String requiredAuthority) {
        return userAuthorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(String::toUpperCase)
                .anyMatch(authority -> authority.equals(requiredAuthority.toUpperCase()));
    }

    public static GrantedAuthority getAuthority(String authority) {
        return AUTHORITIES.get(authority.toUpperCase());
    }
}
