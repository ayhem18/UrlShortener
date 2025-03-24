package org.apiUtils.commonClasses;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.tokens.entities.TokenUserLink;
import org.tokens.repositories.TokenUserLinkRepository;
import org.user.entities.AppUser;
import org.user.repositories.UserRepository;


public class TokenController {
        
    public static class TokenNotFoundException extends RuntimeException {
        public TokenNotFoundException(String message) {
            super(message);
        }
    }


    private final UserRepository userRepo;
    private final TokenUserLinkRepository tokenUserLinkRepo;
    
    public TokenController(UserRepository userRepo, TokenUserLinkRepository tokenUserLinkRepo) {
        this.userRepo = userRepo;
        this.tokenUserLinkRepo = tokenUserLinkRepo;
    }
    
    
    public AppUser validateUserToken(UserDetails userDetails) {
        AppUser user = this.userRepo.findById(userDetails.getUsername()).get();
        // look for tokens 
        List<TokenUserLink> tokenUserLinks = this.tokenUserLinkRepo.findByUser(user);

        if (tokenUserLinks.isEmpty()) {
            throw new TokenNotFoundException("The user is currently associated with no tokens. His access might have been revoked.");
        }

        return user;
    }


}
