package org.access;

import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class RoleManager {
    public static final List<Role> ROLES = List.of(new Owner(), new Admin(), new Employee());
    private static final Map<String, Role> ROLES_MAP = new HashMap<>();

    
    public static final String OWNER_ROLE = "owner";
    public static final String ADMIN_ROLE = "admin";
    public static final String EMPLOYEE_ROLE = "employee";

    static {
        ROLES_MAP.put(OWNER_ROLE, ROLES.getFirst());
        ROLES_MAP.put(ADMIN_ROLE, ROLES.get(1));
        ROLES_MAP.put(EMPLOYEE_ROLE, ROLES.getLast());
    }
    
    public static final List<String> ROLES_STRING = List.of(OWNER_ROLE, ADMIN_ROLE, EMPLOYEE_ROLE);

    public static class NoExistingRoleException extends RuntimeException{
        public NoExistingRoleException(String message) {
            super(message);
        }
    }

    public static Role getRole(String roleString) throws NoExistingRoleException {
        if (!ROLES_MAP.containsKey(roleString.toLowerCase())) {
            throw new NoExistingRoleException("The role " + roleString + " is not yet supported");
        }
        return ROLES_MAP.get(roleString.toLowerCase());
    }

}
