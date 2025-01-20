package org.common;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public class AuthoritiesManager {
    private static final List<GrantedAuthority> AUTHS = List.of(
            new canUpdateCompanyDetails(),
            new canViewCompanyDetails(),
            new CanPay(),
            new CanViewStats(),
            new CanUseShortUrl(),
            new CanEditUrl()
    );

    public static final String CAN_UPDATE_COMPANY_DETAILS = AUTHS.getFirst().getAuthority();
    public static final String CAN_VIEW_COMPANY_DETAILS = AUTHS.get(1).getAuthority();
    public static final String CAN_PAY_AUTH = AUTHS.get(1).getAuthority();
}
