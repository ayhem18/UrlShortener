package org.tokens.repositories;

import org.access.Role;
import org.company.entities.Company;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.tokens.entities.AppToken;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends MongoRepository<AppToken, String> {
    @SuppressWarnings("null")
    Optional<AppToken> findById(String id);
    
    Optional<AppToken> findByTokenId(String tokenId);
    
    List<AppToken> findByTokenState(AppToken.TokenState tokenState);

    List<AppToken> findByCompany(Company company);
    
    List<AppToken> findByCompanyAndRole(Company company, Role role);

    long countByCompanyAndRole(Company company, Role role);

    List<AppToken> findByCompanyAndRoleIn(Company company, List<Role> roles);

    List<AppToken> findByCompanyAndRoleIn(Company company, List<Role> roles, Sort sort);

    List<AppToken> findByCompanyAndRole(Company company, Role role, Sort sort);

    
    List<AppToken> findByCompanyAndTokenState(Company company, AppToken.TokenState tokenState);
    
    List<AppToken> findByCompanyAndRoleAndTokenState(Company company, Role role, AppToken.TokenState tokenState);

} 