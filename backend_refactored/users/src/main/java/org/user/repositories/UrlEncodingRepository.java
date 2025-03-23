package org.user.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.user.entities.AppUser;
import org.user.entities.UrlEncoding;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UrlEncodingRepository extends MongoRepository<UrlEncoding, String> {
    @SuppressWarnings("null")
    List<UrlEncoding> findAll();

    // Basic pagination and sorting methods
    @SuppressWarnings("null")
    Page<UrlEncoding> findAll(Pageable pageable);
    
    // Find by user with pagination and sorting
    Page<UrlEncoding> findByUser(AppUser user, Pageable pageable);

    List<UrlEncoding> findByUser(AppUser user);
    
    // this feature is used to find the most recent n url encodings for a given user
    Page<UrlEncoding> findByUserAndUrlEncodingCountGreaterThan(AppUser user, int urlEncodingCount, Pageable pageable);

    List<UrlEncoding> findByUserAndUrlEncodingTimeAfter(AppUser user, LocalDateTime time);

}
