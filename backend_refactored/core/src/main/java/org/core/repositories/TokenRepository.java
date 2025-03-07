package org.core.repositories;

import org.core.entities.Token;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends MongoRepository<Token, String> {

    // find a token by its id
    Optional<Token> findById(String id);
    
    // Find all tokens for a specific company
    List<Token> findByCompanyId(String companyId);

    // Delete tokens by company ID
    void deleteByCompanyId(String companyId);
} 