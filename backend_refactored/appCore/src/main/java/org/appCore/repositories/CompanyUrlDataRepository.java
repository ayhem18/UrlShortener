package org.appCore.repositories;


import org.appCore.entities.Company;
import org.appCore.entities.CompanyUrlData;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CompanyUrlDataRepository extends MongoRepository<CompanyUrlData, ObjectId> {
    Optional<CompanyUrlData> findByCompany(Company company);
}
