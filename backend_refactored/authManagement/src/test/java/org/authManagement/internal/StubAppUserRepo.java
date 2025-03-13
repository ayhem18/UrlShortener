package org.authManagement.internal;

import org.access.RoleManager;
import org.company.entities.Company;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.user.entities.AppUser;
import org.user.repositories.UserRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "null", "NullableProblems", "ConstantConditions"})
public class StubAppUserRepo implements UserRepository {
    private final List<AppUser> db;
    private final Map<String, List<AppUser>> roleDb;
    private final PasswordEncoder encoder;
    private final StubCompanyRepo companyRepo;

    public StubAppUserRepo(StubCompanyRepo companyRepo) {
        this.db = new ArrayList<>();
        this.roleDb = new HashMap<>();
        this.encoder = new BCryptPasswordEncoder();
        this.companyRepo = companyRepo;
    }

    public PasswordEncoder getEncoder() {
        return encoder;
    }

    public List<AppUser> getDb() {
        return db;
    }

    public Map<String, List<AppUser>> getRoleDb() {
        return roleDb;
    }

    public void addDefaultUsers() {
        if (!companyRepo.getDb().isEmpty()) {
            Company c1 = companyRepo.getDb().getFirst();
            Company c2 = companyRepo.getDb().get(1);
            
            // Create users with different roles
            AppUser ownerC1 = new AppUser("owner1@example.com", "owner1", encoder.encode("password1"), c1, RoleManager.getRole("owner"));
            AppUser adminC1 = new AppUser("admin1@example.com", "admin1", encoder.encode("password2"), c1, RoleManager.getRole("admin"));
            AppUser ownerC2 = new AppUser("owner2@example.com", "owner2", encoder.encode("password3"), c2, RoleManager.getRole("owner"));
            
            // Add to role-based map
            this.roleDb.put("owner", new ArrayList<>(List.of(ownerC1, ownerC2)));
            this.roleDb.put("admin", new ArrayList<>(List.of(adminC1)));
            
            // Add to main db
            this.db.addAll(List.of(ownerC1, adminC1, ownerC2));
        }
    }

    @Override
    public Optional<AppUser> findById(String id) {
        return this.db.stream()
                .filter(user -> user.getUsername().equals(id))
                .findFirst();
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
        return this.db.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public boolean existsById(String id) {
        return this.db.stream().anyMatch(user -> user.getUsername().equals(id));
    }

    @Override
    public <S extends AppUser> S save(S entity) {
        for (int i = 0; i < this.db.size(); i++) {
            if (this.db.get(i).getUsername().equals(entity.getUsername())) {
                this.db.set(i, entity);
                
                // Update role map
                String role = entity.getRole().toString();
                for (Map.Entry<String, List<AppUser>> entry : this.roleDb.entrySet()) {
                    entry.getValue().removeIf(user -> user.getUsername().equals(entity.getUsername()));
                }
                
                this.roleDb.computeIfAbsent(role, k -> new ArrayList<>()).add(entity);
                return entity;
            }
        }
        
        // If not found, add the new entity
        this.db.add(entity);
        
        // Add to role map
        String role = entity.getRole().toString();
        this.roleDb.computeIfAbsent(role, k -> new ArrayList<>()).add(entity);
        
        return entity;
    }

    @Override
    public void deleteAll() {
        this.db.clear();
        this.roleDb.clear();
    }

    @Override
    public void deleteAll(Iterable<? extends AppUser> entities) {
        this.db.clear();
        this.roleDb.clear();
    }

    @Override
    public long count() {
        return this.db.size();
    }

    // Stub implementations for other required methods
    @Override
    public <S extends AppUser> S insert(S entity) { return null; }
    @Override
    public <S extends AppUser> List<S> insert(Iterable<S> entities) { return List.of(); }
    @Override
    public <S extends AppUser> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
    @Override
    public <S extends AppUser> List<S> findAll(Example<S> example) { return List.of(); }
    @Override
    public <S extends AppUser> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
    @Override
    public <S extends AppUser> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
    @Override
    public <S extends AppUser> long count(Example<S> example) { return 0; }
    @Override
    public <S extends AppUser> boolean exists(Example<S> example) { return false; }
    @Override
    public <S extends AppUser, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
    @Override
    public <S extends AppUser> List<S> saveAll(Iterable<S> entities) { return List.of(); }
    @Override
    public List<AppUser> findAll() { return new ArrayList<>(this.db); }
    @Override
    public List<AppUser> findAllById(Iterable<String> ids) { return List.of(); }
    @Override
    public void deleteById(String id) {}
    @Override
    public void delete(AppUser entity) {}
    @Override
    public void deleteAllById(Iterable<? extends String> ids) {}
    @Override
    public List<AppUser> findAll(Sort sort) { return List.of(); }
    @Override
    public Page<AppUser> findAll(Pageable pageable) { return null; }
} 