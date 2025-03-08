package org.appCore.repositories;

import org.appCore.entities.Company;
import org.appCore.entities.CompanyUrlData;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyUrlDataRepository extends MongoRepository<CompanyUrlData, ObjectId> {
    Optional<CompanyUrlData> findByCompany(Company company);
}
