package org.tokens.repositories;

import org.tokens.entities.Token;
import org.tokens.entities.TokenUserLink;
import org.user.entities.AppUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenUserLinkRepository extends MongoRepository<TokenUserLink, String> {
    @SuppressWarnings("null")
    Optional<TokenUserLink> findById(String id);
    
    List<TokenUserLink> findByUser(AppUser user);
    
    List<TokenUserLink> findByToken(Token token);
    
    Optional<TokenUserLink> findByUserAndToken(AppUser user, Token token);
    
    List<TokenUserLink> findByActivationTimeBefore(LocalDateTime dateTime);
    
    List<TokenUserLink> findByDeactivationTimeIsNull();
    
    List<TokenUserLink> findByUserAndDeactivationTimeIsNull(AppUser user);
} 