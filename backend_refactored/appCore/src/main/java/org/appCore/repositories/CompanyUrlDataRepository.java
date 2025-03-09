package org.appCore.repositories;

import org.appCore.entities.CompanyUrlData;
import org.bson.types.ObjectId;
import org.company.entities.Company;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// for MongoDB repositories to be recognized as beans (in different subprojects), the @EnableMongoRepositories annotation must be added
// to the main app configuration file. (generally the one with the @SpringBootApplication annotation)
@Repository
public interface CompanyUrlDataRepository extends MongoRepository<CompanyUrlData, ObjectId> {
    Optional<CompanyUrlData> findByCompany(Company company);
}
