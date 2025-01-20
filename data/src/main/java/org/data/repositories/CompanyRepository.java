package org.data.repositories;

import org.data.entities.Company;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;



//@Configuration I still do not understand why it is not necessary to associate the Repository interface
// with the @Configuration / @Bean annotations. (maybe because it is an interface / abstract class)
public interface CompanyRepository extends MongoRepository<Company, String> {
    Optional<Company> findById(String id);
    Optional<Company> findBySite(String site);
}
