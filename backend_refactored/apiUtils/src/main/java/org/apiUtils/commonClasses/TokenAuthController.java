package org.apiUtils.commonClasses;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.tokens.entities.TokenUserLink;
import org.tokens.repositories.TokenUserLinkRepository;
import org.user.entities.AppUser;
import org.user.repositories.UserRepository;

@RestController
@Validated
public class TokenAuthController {
        
    public static class TokenNotFoundException extends RuntimeException {
        public TokenNotFoundException(String message) {
            super(message);
        }
    }


    protected final UserRepository userRepo;
    protected final TokenUserLinkRepository tokenUserLinkRepo;
    
    public TokenAuthController(UserRepository userRepo, TokenUserLinkRepository tokenUserLinkRepo) {
        this.userRepo = userRepo;
        this.tokenUserLinkRepo = tokenUserLinkRepo;
    }
    
    
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public AppUser authorizeUserToken(UserDetails userDetails) {
        AppUser user = this.userRepo.findById(userDetails.getUsername()).get();
        // look for tokens 
        List<TokenUserLink> tokenUserLinks = this.tokenUserLinkRepo.findByUser(user);

        if (tokenUserLinks.isEmpty()) {
            throw new TokenNotFoundException("The user is currently associated with no tokens. His access might have been revoked.");
        }

        return user;
    }
}
