package com.url_shortener.User;


import org.springframework.security.core.GrantedAuthority;

import java.util.List;


class CanPay implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return "canPay";
    }
}

class CanViewStats implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return "CanViewStats";
    }
}

class CanEditUrl implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return "CanEditUrl";
    }
}

class CanUseShortUrl implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return "canUseShortUrl";
    }
}


public interface Role {
    List<GrantedAuthority> getAuthorities();

    String toString();
}

class Owner implements Role {
    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(new CanPay(), new CanViewStats(), new CanEditUrl(), new CanUseShortUrl());
    }

    @Override
    public String toString() {
        return "Owner";
    }
}

class Admin implements Role {
    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(new CanViewStats(), new CanEditUrl(), new CanUseShortUrl());
    }

    @Override
    public String toString() {
        return "Admin";
    }
}

class RegisteredUser implements Role {
    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(new CanUseShortUrl());
    }

    @Override
    public String toString() {
        return "RegisterUser";
    }
}

