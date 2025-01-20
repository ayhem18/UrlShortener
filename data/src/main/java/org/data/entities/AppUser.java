package org.data.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.common.Role;
import org.data.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


import java.util.Collection;


@Document()
public class AppUser {

    @Id
    private String username;

    // the password should be written :Json to obj, but not read: obj to json
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    // each user is associated with a company (a specific site)
    @DocumentReference
    private Company company;

    // the role the user plays in this company (determines the authorities !!)
    private Role role;

    public AppUser(String userName, String password, Company company, Role role) {
        this.username = userName;
        this.password = password;
        this.company = company;
        this.role = role;
    }

    public AppUser() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "username='" + username + '\'' +
                ", company=" + company.getId() +
                ", role=" + role +
                '}';
    }
}


class UserDetailsImp implements UserDetails {

    private final AppUser user;

    public UserDetailsImp(AppUser user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        System.out.println("\nListing authorities\n");
        this.user.getRole().getAuthorities().forEach(auth -> System.out.println(auth.getAuthority()));
        System.out.println("\n");
        return this.user.getRole().getAuthorities();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }


}



@Component
class AppUserDetailService implements UserDetailsService {
    private final UserRepository userRepo;

    @Autowired
    public AppUserDetailService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepo.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("There is no user with the username: " + username)
        );
        return new UserDetailsImp(user);
    }
}
