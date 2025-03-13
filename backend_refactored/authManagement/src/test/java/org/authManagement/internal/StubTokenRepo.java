package org.authManagement.internal;

import org.access.Role;
import org.access.RoleManager;
import org.company.entities.Company;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tokens.entities.AppToken;
import org.tokens.entities.AppToken.TokenState;
import org.tokens.repositories.TokenRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "null", "NullableProblems", "ConstantConditions"})
public class StubTokenRepo implements TokenRepository {
    private final List<AppToken> db;
    private final PasswordEncoder encoder;
    private final StubCompanyRepo companyRepo;

    public StubTokenRepo(StubCompanyRepo companyRepo) {
        this.db = new ArrayList<>();
        this.encoder = new BCryptPasswordEncoder();
        this.companyRepo = companyRepo;
    }

    public PasswordEncoder getEncoder() {
        return encoder;
    }

    public List<AppToken> getDb() {
        return db;
    }

    public void addDefaultTokens() {
        Company c1 = this.companyRepo.getDb().getFirst();
        Company c2 = this.companyRepo.getDb().get(1);

        // Create tokens for different roles for each company
        AppToken ownerTokenC1 = new AppToken("token1", encoder.encode("ownertoken1"), c1, RoleManager.getRole("owner"));
        AppToken adminTokenC1 = new AppToken("token2", encoder.encode("admintoken1"), c1, RoleManager.getRole("admin"));
        AppToken ownerTokenC2 = new AppToken("token3", encoder.encode("ownertoken2"), c2, RoleManager.getRole("owner"));
        AppToken adminTokenC2 = new AppToken("token4", encoder.encode("admintoken2"), c2, RoleManager.getRole("admin"));

        // Set some tokens to different states
        ownerTokenC1.activate();
        
//        // Add expiration time to some tokens
//        adminTokenC2.expire();

        this.db.addAll(List.of(ownerTokenC1, adminTokenC1, ownerTokenC2, adminTokenC2));
    }

    @Override
    public Optional<AppToken> findById(String id) {
        return this.db.stream()
                .filter(token -> token.getTokenId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<AppToken> findByTokenId(String tokenId) {
        return this.db.stream()
                .filter(token -> token.getTokenId().equals(tokenId))
                .findFirst();
    }

    @Override
    public List<AppToken> findByTokenState(TokenState tokenState) {
        return this.db.stream()
                .filter(token -> token.getTokenState() == tokenState)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppToken> findByCompany(Company company) {
        return this.db.stream()
                .filter(token -> token.getCompany() != null && token.getCompany().getId().equals(company.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<AppToken> findByCompanyAndRole(Company company, Role role) {
        return this.db.stream()
                .filter(token -> token.getCompany() != null && 
                        token.getCompany().getId().equals(company.getId()) && 
                        token.getRole() != null && 
                        token.getRole().toString().equals(role.toString()))
                .collect(Collectors.toList());
    }

    @Override
    public List<AppToken> findByCompanyAndTokenState(Company company, TokenState tokenState) {
        return this.db.stream()
                .filter(token -> token.getCompany() != null && 
                        token.getCompany().getId().equals(company.getId()) && 
                        token.getTokenState() == tokenState)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppToken> findByCompanyAndRoleAndTokenState(Company company, Role role, TokenState tokenState) {
        return this.db.stream()
                .filter(token -> token.getCompany() != null && 
                        token.getCompany().getId().equals(company.getId()) && 
                        token.getRole() != null && 
                        token.getRole().toString().equals(role.toString()) && 
                        token.getTokenState() == tokenState)
                .collect(Collectors.toList());
    }

    // Implement the basic MongoRepository methods
    @Override
    public <S extends AppToken> S save(S entity) {
        for (int i = 0; i < this.db.size(); i++) {
            if (this.db.get(i).getTokenId().equals(entity.getTokenId())) {
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
        return this.db.stream().anyMatch(token -> token.getTokenId().equals(id));
    }

    // Stub implementations for other required methods
    @Override
    public <S extends AppToken> S insert(S entity) { return null; }
    @Override
    public <S extends AppToken> List<S> insert(Iterable<S> entities) { return List.of(); }
    @Override
    public <S extends AppToken> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
    @Override
    public <S extends AppToken> List<S> findAll(Example<S> example) { return List.of(); }
    @Override
    public <S extends AppToken> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
    @Override
    public <S extends AppToken> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
    @Override
    public <S extends AppToken> long count(Example<S> example) { return 0; }
    @Override
    public <S extends AppToken> boolean exists(Example<S> example) { return false; }
    @Override
    public <S extends AppToken, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
    @Override
    public <S extends AppToken> List<S> saveAll(Iterable<S> entities) { return List.of(); }
    @Override
    public List<AppToken> findAll() { return new ArrayList<>(this.db); }
    @Override
    public List<AppToken> findAllById(Iterable<String> ids) { return List.of(); }
    @Override
    public long count() { return this.db.size(); }
    @Override
    public void deleteById(String id) {}
    @Override
    public void delete(AppToken entity) {}
    @Override
    public void deleteAllById(Iterable<? extends String> ids) {}
    @Override
    public void deleteAll(Iterable<? extends AppToken> entities) {this.db.clear();}
    @Override
    public void deleteAll() { this.db.clear(); }
    @Override
    public List<AppToken> findAll(Sort sort) { return List.of(); }
    @Override
    public Page<AppToken> findAll(Pageable pageable) { return null; }
} 