package org.apiUtils.repositories;

import org.apiUtils.entities.CollectionCounter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;



@Repository
public interface CounterRepository extends MongoRepository<CollectionCounter, String> {
    Optional<CollectionCounter> findById(String id);

    boolean existsById(String id);

    Optional<CollectionCounter> findByCollectionName(String collectionName);

    boolean existsByCollectionName(String collectionName);

    // since the counter is usually used to determine ids of new objects,
    // it should be treated as a transactional operation
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Transactional
    default long nextId(String collectionName) {
        if (! this.existsByCollectionName(collectionName)) {
            CollectionCounter c = new CollectionCounter(collectionName);
            this.save(c);
            // first object created
            c.setCount(1);
            this.save(c);
            return 0;
        }
        CollectionCounter c = this.findByCollectionName(collectionName).get();
        long count = c.getCount();
        c.setCount(count + 1);
        this.save(c);
        return count;
    }
}
