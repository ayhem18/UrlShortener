package org.appCore.repositories;

import org.appCore.entities.CollectionCounter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Repository
public interface CounterRepository extends MongoRepository<CollectionCounter, String> {
    @SuppressWarnings("null")
    Optional<CollectionCounter> findById(String id);

    // since the counter is usually used to determine ids of new objects,
    // it should be treated as a transactional operation
    @Transactional
    default long getCount(String collectionId) {
        if (! this.existsById(collectionId)) {
            CollectionCounter c = new CollectionCounter(collectionId);
            this.save(c);
            // first object created
            c.setCount(1);
            return 0;
        }
        CollectionCounter c = this.findById(collectionId).get();
        c.setCount(c.getCount() + 1);
        this.save(c);
        return c.getCount() - 1;
    }
}
