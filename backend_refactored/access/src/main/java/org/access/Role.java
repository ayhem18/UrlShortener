
package org.access;

import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;



public interface Role {
    List<GrantedAuthority> getAuthorities();
    
    @JsonValue
    String role();
}


class Owner implements Role {   
    private static final String ROLE_NAME = "OWNER";

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

    @Override
    public String role() {
        return ROLE_NAME;
    }

    @Override
    public String toString() {
        return ROLE_NAME.toLowerCase();
    }
}

class Admin implements Role {
    private static final String ROLE_NAME = "ADMIN";

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(
                new CanViewCompanyDetails(),
                new CanUseShortUrl(),
                new CanEncodeUrl()
        );
    }

    @Override
    public String role() {
        return ROLE_NAME;
    }

    @Override
    public String toString() {
        return ROLE_NAME.toLowerCase();
    }

}

class Employee implements Role {
    private static final String ROLE_NAME = "EMPLOYEE";

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(
                new CanUseShortUrl(),
                new CanEncodeUrl(),
                new CanViewSubscription());
    }

    @Override
    public String role() {
        return ROLE_NAME;
    }

    @Override
    public String toString() {
        return ROLE_NAME.toLowerCase();
    }
}
