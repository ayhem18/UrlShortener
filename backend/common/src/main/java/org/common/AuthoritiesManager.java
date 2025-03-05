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

    public static final String CAN_UPDATE_COMPANY_DETAILS_STR = AUTHS.getFirst().getAuthority();
    public static final GrantedAuthority CAN_UPDATE_COMPANY_DETAILS = AUTHS.getFirst();

    public static final String CAN_VIEW_COMPANY_DETAILS_STR = AUTHS.get(1).getAuthority();
    public static final GrantedAuthority CAN_VIEW_COMPANY_DETAILS = AUTHS.get(1);

    public static final String CAN_PAY_AUTH_STR = AUTHS.get(2).getAuthority();
    public static final GrantedAuthority CAN_PAY_AUTH = AUTHS.get(2);

    public static final String CAN_VIEW_STATS_STR = AUTHS.get(3).getAuthority();
    public static final GrantedAuthority CAN_VIEW_STATS = AUTHS.get(3);

    public static final String CAN_USE_URL_STR = AUTHS.get(4).getAuthority();
    public static final GrantedAuthority CAN_USE_URL = AUTHS.get(4);

    public static final String CAN_EDIT_URL_STR = AUTHS.get(5).getAuthority();
    public static final GrantedAuthority CAN_EDIT_URL = AUTHS.get(5);

}
