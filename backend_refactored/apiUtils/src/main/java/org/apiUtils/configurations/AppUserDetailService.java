package org.apiUtils.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.user.entities.AppUser;
import org.user.repositories.UserRepository;


@Component
@SuppressWarnings("unused")
public class AppUserDetailService implements UserDetailsService {
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
