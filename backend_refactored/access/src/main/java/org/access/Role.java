
package org.access;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;


public interface Role {
    List<GrantedAuthority> getAuthorities();
}


class Owner implements Role {
    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(
            new CanUpdateDomainName(),
            new CanCancelSubscription(),
            new CanGenerateTokens(),

            new CanViewCompanyDetails(),
            new CanViewSubscription(),
            new CanEncodeUrl(),
            new CanUseShortUrl()
    );
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
                new CanViewCompanyDetails(),
                new CanUseShortUrl(),
                new CanEncodeUrl()
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

class Employee implements Role {
    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(
                new CanUseShortUrl(),
                new CanEncodeUrl(),
                new CanViewSubscription());
    }

    public static String role() {
        return "Employee".toLowerCase();
    }

    @Override
    public String toString() {
        return "employee";
    }
}
