package org.authManagement.internal;

import org.company.entities.CompanyUrlData;
import org.company.repositories.CompanyUrlDataRepository;
import org.bson.types.ObjectId;
import org.company.entities.Company;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings({"null", "NullableProblems", "ConstantConditions"})
@Repository // make sure to add the @Repository annotation for the Stub repo to be found by the MockMVC context
public class StubCompanyUrlDataRepo implements CompanyUrlDataRepository {
    private final List<CompanyUrlData> db;

    public StubCompanyUrlDataRepo() {
        this.db = new ArrayList<>();
    }

    @Override
    public Optional<CompanyUrlData> findByCompany(Company company) {
        for (CompanyUrlData data : this.db) {
            if (data.getCompany().equals(company)) {
                return Optional.of(data);
            }
        }
        return Optional.empty();
    }

    @Override
    public <S extends CompanyUrlData> S save(S entity) {
        // Check if entity already exists by company ID
        if (entity.getCompany() != null) {
            for (int i = 0; i < this.db.size(); i++) {
                CompanyUrlData existingData = this.db.get(i);
                if (existingData.getCompany().equals(entity.getCompany())) {
                    // Replace existing entity
                    this.db.set(i, entity);
                    return entity;
                }
            }
        }
        
        // Add new entity if no existing match was found
        this.db.add(entity);
        return entity;
    }

    @Override
    public <S extends CompanyUrlData> List<S> saveAll(Iterable<S> entities) {
        List<S> savedEntities = new ArrayList<>();
        for (S entity : entities) {
            savedEntities.add(save(entity));
        }
        return savedEntities;
    }

    @Override
    public Optional<CompanyUrlData> findById(ObjectId id) {
        // Since we're not setting IDs in the test environment, this is a stub implementation
        // Real implementation would use the ObjectId
        return Optional.empty();
    }

    @Override
    public boolean existsById(ObjectId id) {
        // Since we're not setting IDs in the test environment, always return false
        return false;
    }

    @Override
    public List<CompanyUrlData> findAll() {
        return new ArrayList<>(this.db);
    }

    @Override
    public void deleteAll() {
        this.db.clear();
    }

    // The remaining methods from MongoRepository interface that we don't expect to use in tests
    
    @Override
    public void delete(CompanyUrlData entity) {
        // Not implemented for test stub
    }

    @Override
    public void deleteById(ObjectId id) {
        // Not implemented for test stub
    }

    @Override
    public long count() {
        return this.db.size();
    }
    
    @Override
    public List<CompanyUrlData> findAllById(Iterable<ObjectId> ids) {
        return new ArrayList<>();
    }

    @Override
    public <S extends CompanyUrlData> S insert(S entity) {
        return null;
    }

    @Override
    public <S extends CompanyUrlData> List<S> insert(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public <S extends CompanyUrlData> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends CompanyUrlData> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends CompanyUrlData> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends CompanyUrlData> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends CompanyUrlData> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends CompanyUrlData> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends CompanyUrlData, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public void deleteAllById(Iterable<? extends ObjectId> ids) {
        // Not implemented for test stub
    }

    @Override
    public void deleteAll(Iterable<? extends CompanyUrlData> entities) {
        this.db.clear();
    }

    @Override
    public List<CompanyUrlData> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<CompanyUrlData> findAll(Pageable pageable) {
        return null;
    }
} 