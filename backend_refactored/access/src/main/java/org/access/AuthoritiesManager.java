package org.access;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthoritiesManager {
    private static final Map<String, GrantedAuthority> AUTHORITIES = Map.of(
            "CAN_UPDATE_DOMAIN_NAME", new CanUpdateDomainName(),
            "CAN_VIEW_COMPANY_DETAILS", new CanViewCompanyDetails(),
            "CAN_DELETE_COMPANY", new CanDeleteCompany(),
            "CAN_VIEW_SUBSCRIPTION", new CanViewSubscription(),
            "CAN_WORK_WITH_TOKENS", new CanWorkWithTokens(),
            "CAN_USE_SHORT_URL", new CanUseShortUrl(),
            "CAN_ENCODE_URL", new CanEncodeUrl(),
            "CAN_VIEW_HISTORY", new CanViewHistory(),
            "CAN_WORK_WITH_SUBSCRIPTION", new CanWorkWithSubscription()
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

    
    public static String CAN_ENCODE_URL_STR = getAuthority("CAN_ENCODE_URL").getAuthority();
    public static String CAN_USE_SHORT_URL_STR = getAuthority("CAN_USE_SHORT_URL").getAuthority();
    public static String CAN_VIEW_HISTORY_STR = getAuthority("CAN_VIEW_HISTORY").getAuthority();
    public static String CAN_UPDATE_DOMAIN_NAME_STR = getAuthority("CAN_UPDATE_DOMAIN_NAME").getAuthority();
    public static String CAN_VIEW_COMPANY_DETAILS_STR = getAuthority("CAN_VIEW_COMPANY_DETAILS").getAuthority();
    public static String CAN_VIEW_SUBSCRIPTION_STR = getAuthority("CAN_VIEW_SUBSCRIPTION").getAuthority();
    public static String CAN_WORK_WITH_TOKENS_STR = getAuthority("CAN_WORK_WITH_TOKENS").getAuthority();
    public static String CAN_WORK_WITH_SUBSCRIPTION_STR = getAuthority("CAN_WORK_WITH_SUBSCRIPTION").getAuthority();
    public static String CAN_DELETE_COMPANY_STR = getAuthority("CAN_DELETE_COMPANY").getAuthority();
}
