package org.stubs.repositories;

import org.company.entities.Company;
import org.company.repositories.CompanyRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@SuppressWarnings({"null", "NullableProblems", "ConstantConditions"})
public class StubCompanyRepo implements CompanyRepository {
    private final List<Company> db;

    public StubCompanyRepo() {
        this.db = new ArrayList<>();
    }

    public List<Company> getDb() {
        return db;
    }

    @Override
    public Optional<Company> findById(String id) {
        for (Company c : this.db) {
            if (c.getId().equals(id)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(String id) {
        for (Company c : this.db) {
            if (c.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public <S extends Company> S save(S entity) {
        for (int i = 0; i < this.db.size(); i++) {
            Company c = this.db.get(i);
            if (c.getId().equals(entity.getId())) {

                this.db.set(i, entity);
                return entity;
            }
        }
        // if we reached this point, add the new entity to the list
        this.db.add(entity);
        return entity;
    }


    // the rest was generated automatically and does not currently (26.01.2024) concern me...
    @Override
    public <S extends Company> S insert(S entity) {
        return null;
    }

    @Override
    public <S extends Company> List<S> insert(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public <S extends Company> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends Company> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Company> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends Company> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Company> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Company> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends Company, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends Company> List<S> saveAll(Iterable<S> entities) {
        List<S> saved = new ArrayList<>();
        for (S entity : entities) {
            saved.add(this.save(entity));
        }
        return saved;
    }

    @Override
    public List<Company> findAll() {
        return this.db;
    }

    @Override
    public List<Company> findByCompanyName(String companyName) {
        return this.db.stream()
            .filter(c -> c.getCompanyName().equalsIgnoreCase(companyName))
            .collect(Collectors.toList());
    }

    @Override
    public List<Company> findAllById(Iterable<String> strings) {
        return List.of();
    }

    @Override
    public long count() {
        return this.db.size();
    }

    @Override
    public void deleteById(String s) {
    }

    @Override
    public void delete(Company entity) {
        this.db.remove(entity);
    }

    @Override
    public void deleteAllById(Iterable<? extends String> strings) {

    }

    @Override
    public void deleteAll(Iterable<? extends Company> entities) {
        this.db.clear();
    }

    @Override
    public void deleteAll() {
        this.db.clear();
    }

    @Override
    public List<Company> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<Company> findAll(Pageable pageable) {
        return null;
    }
}
