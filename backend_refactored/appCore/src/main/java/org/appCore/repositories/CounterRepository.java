package org.appCore.repositories;

import org.appCore.entities.CollectionCounter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CounterRepository extends MongoRepository<CollectionCounter, String> {
    @SuppressWarnings("null")
    Optional<CollectionCounter> findById(String id);
}
