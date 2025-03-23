package org.user.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.user.entities.AppUser;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<AppUser, String> {
    Optional<AppUser> findById(String id);

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmail(String email);
}
