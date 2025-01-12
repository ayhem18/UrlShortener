package com.url_shortener.Service;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public class AuthoritiesManager {
    private static final List<GrantedAuthority> AUTHS = List.of(
            new canUpdateCompany(),
            new CanPay(),
            new CanViewStats(),
            new CanUseShortUrl(),
            new CanEditUrl()
    );

    public static final String CAN_UPDATE_COMPANY = AUTHS.getFirst().getAuthority();
    public static final String CAN_PAY_AUTH = AUTHS.get(1).getAuthority();
}
