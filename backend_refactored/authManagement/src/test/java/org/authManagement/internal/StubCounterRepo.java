package org.authManagement.internal;

import org.authManagement.entities.CollectionCounter;
import org.authManagement.repositories.CounterRepository;
import org.company.entities.Company;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings({"unused", "null", "NullableProblems", "ConstantConditions"})
public class StubCounterRepo implements CounterRepository {
    private final List<CollectionCounter> db;

    public StubCounterRepo() {
        this.db = new ArrayList<>();
    }

    public void addCompanyCollection() {
        this.db.add(new CollectionCounter(Company.COMPANY_COLLECTION_NAME));
    }

    @Override
    public Optional<CollectionCounter> findById(String id) {
        for (CollectionCounter c : this.db) {
            if (c.getCollectionName().equals(id)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(String id) {
        for (CollectionCounter c : this.db) {
            if (c.getCollectionName().equals(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <S extends CollectionCounter> S save(S entity) {
        for (int i = 0; i < this.db.size(); i++) {
            if (this.db.get(i).getCollectionName().equals(entity.getCollectionName())) {
                this.db.set(i, entity);
                return entity;
            }
        }
        // at this point just add the entity
        this.db.add(entity);
        return entity;
    }

    @Override
    public <S extends CollectionCounter> S insert(S entity) {
        return null;
    }

    @Override
    public <S extends CollectionCounter> List<S> insert(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public <S extends CollectionCounter> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends CollectionCounter> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends CollectionCounter> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends CollectionCounter> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends CollectionCounter> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends CollectionCounter> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends CollectionCounter, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }


    @Override
    public <S extends CollectionCounter> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public List<CollectionCounter> findAll() {
        return List.of();
    }

    @Override
    public List<CollectionCounter> findAllById(Iterable<String> strings) {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(String s) {

    }

    @Override
    public void delete(CollectionCounter entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends String> strings) {

    }

    @Override
    public void deleteAll(Iterable<? extends CollectionCounter> entities) {
        this.db.clear();
    }

    @Override
    public void deleteAll() {

    }

    @Override
    public List<CollectionCounter> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<CollectionCounter> findAll(Pageable pageable) {
        return null;
    }
}
