package org.appCore.repositories;

// need userRepository class to save the application users

import org.access.Role;
import org.appCore.entities.AppUser;
import org.appCore.entities.Company;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface  UserRepository extends MongoRepository<AppUser, String> {
    Optional<AppUser> findById(String id);
    Optional<AppUser> findByUsername(String id);

    List<AppUser> findByCompanyAndRole(Company company, Role role);

    List<AppUser> findByCompany(Company company);

    // deletes all users of a given company
    void deleteByCompany(Company company);
}
