package org.core.repositories;

import org.core.entities.Company;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends MongoRepository<Company, String> {
    // find a company by its id    
    Optional<Company> findById(String id);

    // Find company by top level domain
    List<Company> findByTopLevelDomain(String topLevelDomain);

    // Check if a company exists with a given domain
    boolean existsByTopLevelDomain(String topLevelDomain);

    // check if a company exists by its id
    boolean existsById(String id); 
}