package org.appCore.repositories;

import org.appCore.entities.CollectionCounter;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface CounterRepository extends MongoRepository<CollectionCounter, String> {
    Optional<CollectionCounter> findById(String id);
}
