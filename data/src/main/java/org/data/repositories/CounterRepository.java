package org.data.repositories;

import org.data.entities.CollectionCounter;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface CounterRepository extends MongoRepository<CollectionCounter, String> {
    Optional<CollectionCounter> findById(String id);
}
