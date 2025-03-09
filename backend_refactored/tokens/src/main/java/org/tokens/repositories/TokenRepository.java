package org.tokens.repositories;

import org.tokens.entities.AppToken;
import org.tokens.entities.AppToken.TokenState;
import org.access.Role;
import org.company.entities.Company;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends MongoRepository<AppToken, String> {
    @SuppressWarnings("null")
    Optional<AppToken> findById(String id);
    
    Optional<AppToken> findByTokenId(String tokenId);
    
    List<AppToken> findByTokenState(TokenState tokenState);

    List<AppToken> findByCompany(Company company);
    
    List<AppToken> findByCompanyAndRole(Company company, Role role);
    
    List<AppToken> findByCompanyAndTokenState(Company company, TokenState tokenState);
    
    List<AppToken> findByCompanyAndRoleAndTokenState(Company company, Role role, TokenState tokenState);
} 