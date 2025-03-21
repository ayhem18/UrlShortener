package org.data.repositories;

import org.data.entities.Company;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;



// MongoRepository interface is most likely associated with an annotation that inherits @Bean or @Configuration
// hence no needed for adding an annotation myself
public interface CompanyRepository extends MongoRepository<Company, String> {
    Optional<Company> findById(String id);
    Optional<Company> findBySite(String site);
}
