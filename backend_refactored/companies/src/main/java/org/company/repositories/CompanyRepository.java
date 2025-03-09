package org.company.repositories;

import org.company.entities.Company;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;



@Repository
public interface CompanyRepository extends MongoRepository<Company, String> {
    @SuppressWarnings("null")
    Optional<Company> findById(String id);
    Optional<Company> findByTopLevelDomain(String topLevelDomain);
}
