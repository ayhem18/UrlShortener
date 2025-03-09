package org.tokens.repositories;

import org.tokens.entities.Token;
import org.tokens.entities.Token.TokenState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends MongoRepository<Token, String> {
    @SuppressWarnings("null")
    Optional<Token> findById(String id);
    
    Optional<Token> findByTokenId(String tokenId);
    
    List<Token> findByTokenState(TokenState tokenState);
} 