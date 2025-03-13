package org.authManagement.internal;

import org.access.RoleManager;
import org.company.entities.Company;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.user.entities.AppUser;
import org.user.repositories.UserRepository;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings({"unused", "null", "NullableProblems", "ConstantConditions"})
public class StubUserRepo implements UserRepository {

    private final StubCompanyRepo companyRepo;
    private final Map<String, List<AppUser>> roleDb;
    private final List<AppUser> db;

    public StubUserRepo(StubCompanyRepo companyRepo) {
        this.companyRepo = companyRepo;
        this.db = new ArrayList<>();
        this.roleDb = new HashMap<>();
    }

    public Map<String, List<AppUser>> getRoleDb() {
        return roleDb;
    }

    public List<AppUser> getDb() {
        return db;
    }

    public void addOwners() {
        Company c1 = this.companyRepo.getDb().getFirst();
        Company c2 = this.companyRepo.getDb().get(1);

        // create the owners of c1 and c2
        AppUser ownerC1 = new AppUser("ownerc1@gmail.com", "ownerC1", "o_password1", c1, RoleManager.getRole("owner"));
        AppUser ownerC2 = new AppUser("ownerc2@gmail.com", "ownerC2", "o_password2", c2, RoleManager.getRole("owner"));

        this.roleDb.put("owner", List.of(ownerC1, ownerC2));
        this.db.addAll(List.of(ownerC1, ownerC2));
    }

    public void addAdmins() {
        Company c1 = this.companyRepo.getDb().getFirst();
        Company c2 = this.companyRepo.getDb().get(1);

        // create the owners of c1 and c2
        AppUser adminC1 = new AppUser("adminc1@gmail.com", "adminC1", "a_password1", c1, RoleManager.getRole("admin"));
        AppUser adminC2 = new AppUser("adminc2@gmail.com", "adminC2", "a_password2", c2, RoleManager.getRole("admin"));

        this.roleDb.put("admin", List.of(adminC1, adminC2));
        this.db.addAll(List.of(adminC1, adminC2));
    }


    @Override
    public boolean existsById(String id) {
        for (AppUser u : this.db) {
            if (u.getUsername().equals(id)) {
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
        this.db.clear();
        this.roleDb.clear();
    }

    @Override
    public <S extends AppUser> long count(Example<S> example) {
        return this.db.size();
    }


    @Override
    public Optional<AppUser> findById(String id) {
        return Optional.empty();
    }

    @Override
    public Optional<AppUser> findByUsername(String id) {
        return Optional.empty();
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
        return List.of();
    }

    @Override
    public List<AppUser> findAll() {
        return List.of();
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
