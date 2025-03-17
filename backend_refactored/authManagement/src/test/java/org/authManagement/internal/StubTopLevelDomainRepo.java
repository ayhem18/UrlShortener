package org.authManagement.internal;

import org.company.entities.Company;
import org.company.entities.TopLevelDomain;
import org.company.entities.TopLevelDomain.DomainState;
import org.company.repositories.TopLevelDomainRepository;
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
import java.util.stream.Collectors;

@Repository
@SuppressWarnings({"unused", "null", "NullableProblems", "ConstantConditions"})
public class StubTopLevelDomainRepo implements TopLevelDomainRepository {
    private final List<TopLevelDomain> db;
    private final StubCompanyRepo companyRepo;

    public StubTopLevelDomainRepo(StubCompanyRepo companyRepo) {
        this.db = new ArrayList<>();
        this.companyRepo = companyRepo;
    }

    public List<TopLevelDomain> getDb() {
        return db;
    }

    public void addDefaultDomains() {
        if (!companyRepo.getDb().isEmpty()) {
            Company c1 = companyRepo.getDb().getFirst();
            Company c2 = companyRepo.getDb().get(1);
            
            TopLevelDomain domain1 = new TopLevelDomain("id1", "youtube.com", "hash1", c1);
//            domain1.verify();
            
            TopLevelDomain domain2 = new TopLevelDomain("id2", "github.com", "hash2", c2);
//            domain2.verify();
            
            this.db.addAll(List.of(domain1, domain2));
        }
    }

    @Override
    public Optional<TopLevelDomain> findById(String id) {
        return this.db.stream()
                .filter(domain -> domain.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<TopLevelDomain> findByDomain(String domain) {
        return this.db.stream()
                .filter(tld -> tld.getDomain().equals(domain))
                .findFirst();
    }

    @Override
    public List<TopLevelDomain> findByCompany(Company company) {
        return this.db.stream()
                .filter(domain -> domain.getCompany() != null && 
                        domain.getCompany().getId().equals(company.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TopLevelDomain> findByCompanyAndDomainState(Company company, DomainState domainState) {
        return this.db.stream()
                .filter(domain -> domain.getCompany() != null && 
                        domain.getCompany().getId().equals(company.getId()) &&
                        domain.getDomainState() == domainState)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TopLevelDomain> findByDomainAndDomainState(String domain, DomainState domainState) {
        return this.db.stream()
                .filter(tld -> tld.getDomain().equals(domain) && 
                        tld.getDomainState() == domainState)
                .findFirst();
    }

    // Implement the basic MongoRepository methods
    @Override
    public <S extends TopLevelDomain> S save(S entity) {
        for (int i = 0; i < this.db.size(); i++) {
            if (this.db.get(i).getId().equals(entity.getId())) {
                this.db.set(i, entity);
                return entity;
            }
        }
        // If not found, add the new entity
        this.db.add(entity);
        return entity;
    }

    @Override
    public boolean existsById(String id) {
        return this.db.stream().anyMatch(domain -> domain.getId().equals(id));
    }

    // Stub implementations for other required methods
    @Override
    public <S extends TopLevelDomain> S insert(S entity) { return null; }
    @Override
    public <S extends TopLevelDomain> List<S> insert(Iterable<S> entities) { return List.of(); }
    @Override
    public <S extends TopLevelDomain> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
    @Override
    public <S extends TopLevelDomain> List<S> findAll(Example<S> example) { return List.of(); }
    @Override
    public <S extends TopLevelDomain> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
    @Override
    public <S extends TopLevelDomain> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
    @Override
    public <S extends TopLevelDomain> long count(Example<S> example) { return 0; }
    @Override
    public <S extends TopLevelDomain> boolean exists(Example<S> example) { return false; }
    @Override
    public <S extends TopLevelDomain, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
    @Override
    public <S extends TopLevelDomain> List<S> saveAll(Iterable<S> entities) {
        List<S> saved = new ArrayList<>();
        for (S entity : entities) {
            saved.add(this.save(entity));
        }
        return saved;
    }
    @Override
    public List<TopLevelDomain> findAll() { return new ArrayList<>(this.db); }
    @Override
    public List<TopLevelDomain> findAllById(Iterable<String> ids) { return List.of(); }
    @Override
    public long count() { return this.db.size(); }
    @Override
    public void deleteById(String id) {}
    @Override
    public void delete(TopLevelDomain entity) {}
    @Override
    public void deleteAllById(Iterable<? extends String> ids) {}
    @Override
    public void deleteAll(Iterable<? extends TopLevelDomain> entities) {this.db.clear();}
    @Override
    public void deleteAll() { this.db.clear(); }
    @Override
    public List<TopLevelDomain> findAll(Sort sort) { return List.of(); }
    @Override
    public Page<TopLevelDomain> findAll(Pageable pageable) { return null; }
} 