package org.company.repositories;

import org.company.entities.TopLevelDomain;
import org.company.entities.Company;
import org.company.entities.TopLevelDomain.DomainState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopLevelDomainRepository extends MongoRepository<TopLevelDomain, String> {
    @SuppressWarnings("null")
    Optional<TopLevelDomain> findById(String id);
    
    Optional<TopLevelDomain> findByDomain(String domain);
    
    List<TopLevelDomain> findByCompany(Company company);
    
    List<TopLevelDomain> findByCompanyAndDomainState(Company company, DomainState domainState);
    
    Optional<TopLevelDomain> findByDomainAndDomainState(String domain, DomainState domainState);
} 