package org.appCore.repositories;

import org.appCore.entities.Company;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


// MongoRepository interface is most likely associated with an annotation that inherits @Bean or @Configuration
// hence no needed for adding an annotation myself
@Repository
public interface CompanyRepository extends MongoRepository<Company, String> {
    @SuppressWarnings("null")
    Optional<Company> findById(String id);
    Optional<Company> findByTopLevelDomain(String topLevelDomain);
}
