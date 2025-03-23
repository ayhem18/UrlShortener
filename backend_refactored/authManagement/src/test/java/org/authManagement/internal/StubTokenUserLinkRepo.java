package org.authManagement.internal;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;
import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.tokens.repositories.TokenUserLinkRepository;
import org.user.entities.AppUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@SuppressWarnings({"unused", "null", "NullableProblems", "ConstantConditions"})
public class StubTokenUserLinkRepo implements TokenUserLinkRepository {
    private final List<TokenUserLink> db;
    private final StubTokenRepo tokenRepo;
    private final StubUserRepo userRepo;

    public StubTokenUserLinkRepo(StubTokenRepo tokenRepo, StubUserRepo userRepo) {
        this.db = new ArrayList<>();
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
    }

    public List<TokenUserLink> getDb() {
        return db;
    }


    @Override
    public Optional<TokenUserLink> findById(String id) {
        return this.db.stream()
                .filter(link -> link.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<TokenUserLink> findByUser(AppUser user) {
        return this.db.stream()
                .filter(link -> link.getUser() != null && 
                        link.getUser().getEmail().equals(user.getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TokenUserLink> findByToken(AppToken token) {
        return this.db.stream()
                .filter(link -> link.getToken() != null && 
                        link.getToken().getTokenId().equals(token.getTokenId()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TokenUserLink> findByUserAndToken(AppUser user, AppToken token) {
        return this.db.stream()
                .filter(link -> link.getUser() != null && 
                        link.getUser().getUsername().equals(user.getUsername()) &&
                        link.getToken() != null && 
                        link.getToken().getTokenId().equals(token.getTokenId()))
                .findFirst();
    }


    // Implement the basic MongoRepository methods
    @Override
    public <S extends TokenUserLink> S save(S entity) {
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
        return this.db.stream().anyMatch(link -> link.getId().equals(id));
    }

    // Stub implementations for other required methods
    @Override
    public <S extends TokenUserLink> S insert(S entity) { return null; }
    @Override
    public <S extends TokenUserLink> List<S> insert(Iterable<S> entities) { return List.of(); }
    @Override
    public <S extends TokenUserLink> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
    @Override
    public <S extends TokenUserLink> List<S> findAll(Example<S> example) { return List.of(); }
    @Override
    public <S extends TokenUserLink> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
    @Override
    public <S extends TokenUserLink> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
    @Override
    public <S extends TokenUserLink> long count(Example<S> example) { return 0; }
    @Override
    public <S extends TokenUserLink> boolean exists(Example<S> example) { return false; }
    @Override
    public <S extends TokenUserLink, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
    @Override
    public <S extends TokenUserLink> List<S> saveAll(Iterable<S> entities) {
        List<S> saved = new ArrayList<>();
        for (S entity : entities) {
            saved.add(this.save(entity));
        }
        return saved;
    }
    @Override
    public List<TokenUserLink> findAll() { return new ArrayList<>(this.db); }
    @Override
    public List<TokenUserLink> findAllById(Iterable<String> ids) { return List.of(); }
    @Override
    public long count() { return this.db.size(); }
    @Override
    public void deleteById(String id) {}
    @Override
    public void delete(TokenUserLink entity) {}
    @Override
    public void deleteAllById(Iterable<? extends String> ids) {}
    @Override
    public void deleteAll(Iterable<? extends TokenUserLink> entities) {this.db.clear();}
    @Override
    public void deleteAll() { this.db.clear(); }
    @Override
    public List<TokenUserLink> findAll(Sort sort) { return List.of(); }
    @Override
    public Page<TokenUserLink> findAll(Pageable pageable) { return null; }
} 