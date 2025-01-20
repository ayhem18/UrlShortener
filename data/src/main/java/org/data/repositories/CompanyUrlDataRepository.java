package org.data.repositories;

import org.data.entities.Company;
import org.bson.types.ObjectId;
import org.data.entities.CompanyUrlData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CompanyUrlDataRepository extends MongoRepository<CompanyUrlData, ObjectId> {
    Optional<CompanyUrlData> findByCompany(Company company);
}
