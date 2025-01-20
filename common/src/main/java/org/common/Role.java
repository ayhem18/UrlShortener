package org.common;


import org.springframework.security.core.GrantedAuthority;

import java.util.List;


class canUpdateCompanyDetails implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return "can_Update_Company".toUpperCase();
    }
}

class canViewCompanyDetails implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return "can_Update_Company".toUpperCase();
    }
}

class CanPay implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return "can_Pay".toUpperCase();
    }
}

class CanViewStats implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return "Can_View_Stats".toUpperCase();
    }
}

class CanEditUrl implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return "Can_Edit_Url".toUpperCase();
    }
}

class CanUseShortUrl implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return "can_UseShort_Url".toUpperCase();
    }
}


public interface Role {
    List<GrantedAuthority> getAuthorities();
}

class Owner implements Role {
    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(new canUpdateCompanyDetails(),
                new canViewCompanyDetails(),
                new CanPay(),
                new CanViewStats(),
                new CanEditUrl(),
                new CanUseShortUrl());
    }

    public static String role() {
        return "Owner".toLowerCase();
    }

    @Override
    public String toString() {
        return "owner";
    }
}

class Admin implements Role {
    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(
                new canViewCompanyDetails(),
                new CanViewStats(),
                new CanEditUrl(),
                new CanUseShortUrl()
        );
    }

    public static String role() {
        return "Admin".toLowerCase();
    }

    @Override
    public String toString() {
        return "admin";
    }

}

class RegisteredUser implements Role {
    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(new CanUseShortUrl());
    }

    public static String role() {
        return "RegisteredUser".toLowerCase();
    }

    @Override
    public String toString() {
        return "registereduser";
    }
}


