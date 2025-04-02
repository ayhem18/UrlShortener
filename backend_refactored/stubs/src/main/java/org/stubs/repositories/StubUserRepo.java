package org.stubs.repositories;

import org.access.RoleManager;
import org.company.entities.Company;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;
import org.user.entities.AppUser;
import org.user.repositories.UserRepository;

import java.util.*;
import java.util.function.Function;


@Repository
@SuppressWarnings({"null", "NullableProblems", "ConstantConditions"})
public class StubUserRepo implements UserRepository {

    private final StubCompanyRepo companyRepo;
    private final Map<String, List<AppUser>> roleDb;
    private final List<AppUser> db;

    public StubUserRepo(StubCompanyRepo companyRepo) {
        this.companyRepo = companyRepo;
        this.db = new ArrayList<>();
        this.roleDb = new HashMap<>();
    }

    @Override
    public boolean existsById(String id) {
        for (AppUser u : this.db) {
            if (u.getEmail().equals(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <S extends AppUser> S save(S entity) {
        for (int i = 0; i < this.db.size(); i++) {
            if (this.db.get(i).getUsername().equals(entity.getUsername())) {
                this.db.set(i, entity);
                return entity;
            }
        }
        // at this point just add the entity
        this.db.add(entity);
        this.roleDb.computeIfAbsent(entity.getRole().toString(), k -> new ArrayList<>());

        List<AppUser> currentMap = new ArrayList<>(this.roleDb.get(entity.getRole().toString()));
        currentMap.add(entity);
        this.roleDb.put(entity.getRole().toString(), currentMap);
        return entity;
    }

    @Override
    public void deleteAll(Iterable<? extends AppUser> entities) {
        for (AppUser u : entities) {
            this.db.remove(u);
            this.roleDb.get(u.getRole().toString()).remove(u);
        }
    }

    @Override
    public <S extends AppUser> long count(Example<S> example) {
        return this.db.size();
    }


    @Override
    public Optional<AppUser> findById(String id) {
        // iterate through the db and return the user with the given id == email
        for (AppUser u : this.db) {
            if (u.getEmail().equals(id)) {
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
        for (AppUser u : this.db) {
            if (u.getUsername().equals(username)) {
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<AppUser> findByEmail(String email) {
        for (AppUser u : this.db) {
            if (u.getEmail().equals(email)) {
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<AppUser> findByCompany(Company company) {
        return this.db.stream().filter(u -> u.getCompany().equals(company)).toList();
    }

    @Override
    public void deleteByCompany(Company company) {
        this.db.removeIf(u -> u.getCompany().equals(company));
    }

    @Override
    public <S extends AppUser> S insert(S entity) {
        return null;
    }

    @Override
    public <S extends AppUser> List<S> insert(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public <S extends AppUser> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends AppUser> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends AppUser> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends AppUser> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }


    @Override
    public <S extends AppUser> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends AppUser, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends AppUser> List<S> saveAll(Iterable<S> entities) {
        List<S> saved = new ArrayList<>();
        for (S entity : entities) {
            saved.add(this.save(entity));
        }
        return saved;
    }

    @Override
    public List<AppUser> findAll() {
        return this.db;
    }

    @Override
    public List<AppUser> findAllById(Iterable<String> strings) {
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
    public void delete(AppUser entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends String> strings) {

    }

    @Override
    public void deleteAll() {
        this.db.clear();
        this.roleDb.clear();
    }

    @Override
    public List<AppUser> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<AppUser> findAll(Pageable pageable) {
        return null;
    }
}
