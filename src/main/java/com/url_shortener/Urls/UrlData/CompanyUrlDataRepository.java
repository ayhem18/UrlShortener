package com.url_shortener.Urls.UrlData;

import com.url_shortener.Service.Company.Company;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CompanyUrlDataRepository extends MongoRepository<CompanyUrlData, ObjectId> {
    Optional<CompanyUrlData> findByCompany(Company company);
}
