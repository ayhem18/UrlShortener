package org.appCore.repositories;

import org.appCore.entities.Token;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends MongoRepository<Token, String> {

    // find a token by its id
    @SuppressWarnings("null")
    Optional<Token> findById(String id);

    // Find all tokens for a specific company
    List<Token> findByCompanyId(String companyId);

    // Delete tokens by company ID
    void deleteByCompanyId(String companyId);
}