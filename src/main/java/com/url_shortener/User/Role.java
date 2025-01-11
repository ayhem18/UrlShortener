package com.url_shortener.User;


import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;


class CanPay implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return "canPay".toLowerCase();
    }
}

class CanViewStats implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return "CanViewStats".toLowerCase();
    }
}

class CanEditUrl implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return "CanEditUrl".toLowerCase();
    }
}

class CanUseShortUrl implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return "canUseShortUrl".toLowerCase();
    }
}


public interface Role {
    List<GrantedAuthority> getAuthorities();
}

class Owner implements Role {
    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(new CanPay(), new CanViewStats(), new CanEditUrl(), new CanUseShortUrl());
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
        return List.of(new CanViewStats(), new CanEditUrl(), new CanUseShortUrl());
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


