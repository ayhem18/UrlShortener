package org.api.internal;

import org.common.SubscriptionManager;
import org.data.entities.Company;
import org.data.repositories.CompanyRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.utils.CustomGenerator;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("NullableProblems")
public class StubCompanyRepo implements CompanyRepository {
    private final List<Company> db;
    private final PasswordEncoder encoder;

    public StubCompanyRepo() {
        this.db = new ArrayList<>();
        this.encoder = new BCryptPasswordEncoder();
        CustomGenerator gen = new CustomGenerator();

        // add the companies
        Map<String, String> h1 =
                Map.of("owner", "owner_token_1", "admin", "admin_token_1", "registereduser", "register_user_token_1");

        Company c1 = new Company("id1", "www.site1.com", SubscriptionManager.getSubscription("TIER_1"),
                h1,
                this.encoder,
                gen);

        Map<String, String> h2 =
                Map.of("owner", "owner_token_2", "admin", "admin_token_2", "registereduser", "register_user_token_2");

        Company c2 = new Company("id2", "www.site2.com", SubscriptionManager.getSubscription("TIER_INFINITY"),
                h2,
                this.encoder,
                gen);

        db.addAll(List.of(c1, c2));
    }

    public PasswordEncoder getEncoder() {
        return encoder;
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
    public boolean existsById(String s) {
        for (Company c : this.db) {
            if (c.getId().equals(s)) {
                return true;
            }
        }
        return false;
    }



    @Override
    public Optional<Company> findBySite(String site) {
        for (Company c : this.db) {
            if (c.getSite().equals(site)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
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
    public <S extends Company> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Company> List<S> findAll(Example<S> example) {
        return List.of();
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
    public <S extends Company> S save(S entity) {
        return null;
    }

    @Override
    public <S extends Company> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public List<Company> findAll() {
        return List.of();
    }

    @Override
    public List<Company> findAllById(Iterable<String> strings) {
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
    public void delete(Company entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends String> strings) {

    }

    @Override
    public void deleteAll(Iterable<? extends Company> entities) {

    }

    @Override
    public void deleteAll() {

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
