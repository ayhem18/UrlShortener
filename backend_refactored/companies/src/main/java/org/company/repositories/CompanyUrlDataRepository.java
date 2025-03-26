package org.company.repositories;

import org.bson.types.ObjectId;
import org.company.entities.Company;
import org.company.entities.CompanyUrlData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// for MongoDB repositories to be recognized as beans (in different subprojects), the @EnableMongoRepositories annotation must be added
// to the main app configuration file. (generally the one with the @SpringBootApplication annotation)
@Repository
public interface CompanyUrlDataRepository extends MongoRepository<CompanyUrlData, ObjectId> {
    boolean existsById(String id);
    Optional<CompanyUrlData> findFirstByCompany(Company company);
    List<CompanyUrlData> findByCompany(Company company);
    List<CompanyUrlData> findAll();
}
