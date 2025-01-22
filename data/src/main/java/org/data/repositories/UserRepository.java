package org.data.repositories;

// need userRepository class to save the application users

import org.common.Role;
import org.data.entities.AppUser;
import org.data.entities.Company;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface  UserRepository extends MongoRepository<AppUser, String> {
    Optional<AppUser> findById(String id);
    Optional<AppUser> findByUsername(String id);

// the method below does NOT work as expected. the method right below does the trick
//    @Query("{'company': ?0, 'roleString': ?1}")
//    List<AppUser> findRolesInCompany(String companyId, String role);

    List<AppUser> findByCompanyAndRole(Company company, Role role);

    List<AppUser> findByCompany(Company company);


    // deletes all users of a given company
    void deleteByCompany(Company company);
}
