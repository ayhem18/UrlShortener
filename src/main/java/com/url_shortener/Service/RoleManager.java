package com.url_shortener.Service;

import com.url_shortener.Service.User.UndefinedRoleException;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class RoleManager {
    public static final List<Role> ROLES = List.of(new Owner(), new Admin(), new RegisteredUser());
    public static final List<String> ROLES_STRING = List.of(Owner.role(), Admin.role(), RegisteredUser.role());
    private static final Map<String, Role> ROLES_MAP = new HashMap<>();

    public static final String OWNER_ROLE = "owner";
    public static final String ADMIN_ROLE = "admin";
    public static final String REGISTERED_USER_ROLE = "registereduser";

    static {
        ROLES_MAP.put(OWNER_ROLE, ROLES.getFirst());
        ROLES_MAP.put(ADMIN_ROLE, ROLES.get(1));
        ROLES_MAP.put(REGISTERED_USER_ROLE, ROLES.getLast());
    }

    public static Role getRole(String roleString) throws UndefinedRoleException {
        if (!ROLES_MAP.containsKey(roleString.toLowerCase())) {
            throw new UndefinedRoleException("The role " + roleString + " is not yet supported");
        }
        return ROLES_MAP.get(roleString.toLowerCase());
    }

}
