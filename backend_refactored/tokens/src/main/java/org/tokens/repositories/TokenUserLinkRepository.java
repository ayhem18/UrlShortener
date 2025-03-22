package org.tokens.repositories;

import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.user.entities.AppUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenUserLinkRepository extends MongoRepository<TokenUserLink, String> {
    @SuppressWarnings("null")
    Optional<TokenUserLink> findById(String id);
    
    List<TokenUserLink> findByUser(AppUser user);
    
    List<TokenUserLink> findByToken(AppToken token);
    
    Optional<TokenUserLink> findByUserAndToken(AppUser user, AppToken token);
}