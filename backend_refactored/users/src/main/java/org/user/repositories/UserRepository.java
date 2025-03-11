package org.user.repositories;

// need userRepository class to save the application users

import org.access.Role;
import org.company.entities.Company;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.user.entities.AppUser;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<AppUser, String> {
    @SuppressWarnings("null")
    Optional<AppUser> findById(String id);

    Optional<AppUser> findByUsername(String username);

//    List<AppUser> findByCompanyAndRole(Company company, Role role);
//
//    List<AppUser> findByCompany(Company company);
//
//    // deletes all users of a given company
//    void deleteByCompany(Company company);
}
