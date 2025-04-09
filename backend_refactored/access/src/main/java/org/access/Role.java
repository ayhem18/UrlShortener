package org.access;

import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

public interface Role extends Comparable<Role> {
    List<GrantedAuthority> getAuthorities();
    
    @JsonValue
    String role();

    int getPriority();
    
    /**
     * Compares roles based on their priority.
     * Lower priority numbers indicate higher importance.
     * 
     * @param other the role to be compared
     * @return a negative integer if this role has higher priority,
     *         zero if equal priority, or a positive integer if lower priority
     */
    @Override
    default int compareTo(Role other) {
        return -Integer.compare(this.getPriority(), other.getPriority());
    }
    
    /**
     * Checks if this role has higher priority than another role.
     * 
     * @param other the role to compare to
     * @return true if this role has higher priority, false otherwise
     */
    default boolean isHigherPriorityThan(Role other) {
        return this.getPriority() < other.getPriority();
    }
    
    /**
     * Checks if this role has lower priority than another role.
     * 
     * @param other the role to compare to
     * @return true if this role has lower priority, false otherwise
     */
    default boolean isLowerPriorityThan(Role other) {
        return this.getPriority() > other.getPriority();
    }
    
    /**
     * Checks if this role has the same priority as another role.
     * 
     * @param other the role to compare to
     * @return true if both roles have the same priority
     */
    default boolean hasSamePriorityAs(Role other) {
        return this.getPriority() == other.getPriority();
    }
}

class Owner implements Role {   
    private static final String ROLE_NAME = "OWNER";
    private static final int PRIORITY = 0; // Highest priority (lowest number)

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(

            // company-level actions
            new CanDeleteCompany(),
            new CanWorkWithSubscription(),
            new CanUpdateDomainName(),

            // user-level actions
            new CanWorkWithTokens(),

            // informative actions: no editing no posting...
            new CanViewCompanyDetails(),
            new CanViewSubscription(),

            // url encoding authorities
            new CanEncodeUrl(),
            new CanViewHistory(),
            new CanUseShortUrl()
    );
    }

    @Override
    public String role() {
        return ROLE_NAME;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public String toString() {
        return ROLE_NAME.toLowerCase();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Owner;
    }
}

class Admin implements Role {
    private static final String ROLE_NAME = "ADMIN";
    private static final int PRIORITY = 10; // Medium priority

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(
                new CanViewCompanyDetails(),
                new CanUseShortUrl(),
                new CanEncodeUrl(),
                new CanViewHistory(),
                new CanWorkWithTokens(),
                new CanViewSubscription()
        );
    }

    @Override
    public String role() {
        return ROLE_NAME;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public String toString() {
        return ROLE_NAME.toLowerCase();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Admin;
    }
}
class Employee implements Role {
    private static final String ROLE_NAME = "EMPLOYEE";
    private static final int PRIORITY = 20; // Lowest priority (highest number)

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(
                new CanUseShortUrl(),
                new CanEncodeUrl(),
                new CanViewHistory(),
                new CanViewSubscription()
        );
    }

    @Override
    public String role() {
        return ROLE_NAME;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public String toString() {
        return ROLE_NAME.toLowerCase();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Employee;
    }
}
