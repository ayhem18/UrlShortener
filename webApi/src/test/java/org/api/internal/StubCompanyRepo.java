package org.api.internal;

import org.common.Subscription;
import org.common.SubscriptionManager;
import org.data.entities.Company;
import org.data.entities.CompanyWrapper;
import org.data.repositories.CompanyRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.utils.CustomGenerator;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

@SuppressWarnings("NullableProblems")
public class StubCompanyRepo implements CompanyRepository {
    private final List<Company> db;
    private final List<CompanyWrapper> wrappers;
    private final PasswordEncoder encoder;

    public StubCompanyRepo() throws NoSuchFieldException, IllegalAccessException {
        this.db = new ArrayList<>();
        this.wrappers = new ArrayList<>();
        this.encoder = new BCryptPasswordEncoder();
        CustomGenerator gen = new CustomGenerator();

        ///////////////////////////////////////// company 1 /////////////////////////////////////////
        Map<String, String> h1 =
                Map.of("owner", "owner_token_1", "admin", "admin_token_1", "registereduser", "register_user_token_1");

        Subscription sub = SubscriptionManager.getSubscription("TIER_1");
        CompanyWrapper w1 = new CompanyWrapper("aaa", "www.youtube.com", sub, h1, this.encoder,
                gen,
                0);

        Field fc = CompanyWrapper.class.getDeclaredField("company");
        fc.setAccessible(true);
        Company c1 = (Company) fc.get(w1);

        ///////////////////////////////////////// company 2 /////////////////////////////////////////

        Map<String, String> h2 =
                Map.of("owner", "owner_token_2", "admin", "admin_token_2", "registereduser", "register_user_token_2");


        sub = SubscriptionManager.getSubscription("TIER_1");
        CompanyWrapper w2 = new CompanyWrapper("aaa", "www.youtube.com", sub, h2, this.encoder,
                gen,
                0);

        fc = CompanyWrapper.class.getDeclaredField("company");
        fc.setAccessible(true);
        Company c2 = (Company) fc.get(w2);

        this.wrappers.addAll(List.of(w1, w2));
        this.db.addAll(List.of(c1, c2));
    }

    public PasswordEncoder getEncoder() {
        return encoder;
    }

    public List<Company> getDb() {
        return db;
    }

    public List<CompanyWrapper> getWrappers() {
        return wrappers;
    }

    @Override
    public Optional<Company> findById(String id) {
        for (Company c : this.db) {
            Field f;

            try {
                f = c.getClass().getDeclaredField("id");
                f.setAccessible(true);
                String companyId = (String) f.get(c);
                if (companyId.equals(id)) {
                    return Optional.of(c);
                }

            } catch (NoSuchFieldException | IllegalAccessException e ) {
                throw new RuntimeException(e);
            }

        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(String id) {
        for (Company c : this.db) {
            Field f;

            try {
                f = c.getClass().getDeclaredField("id");
                f.setAccessible(true);
                String companyId = (String) f.get(c);
                if (companyId.equals(id)) {
                    return true;
                }

            } catch (NoSuchFieldException | IllegalAccessException e ) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }


    @Override
    public Optional<Company> findBySite(String site) {
        for (Company c : this.db) {
            Field f;

            try {
                f = c.getClass().getDeclaredField("site");
                f.setAccessible(true);
                String companySite = (String) f.get(c);
                if (companySite.equals(site)) {
                    return Optional.of(c);
                }
            }
            catch (NoSuchFieldException | IllegalAccessException e ) {
                throw new RuntimeException(e);
            }
        }
        return Optional.empty();
    }

    @Override
    public <S extends Company> S save(S entity) {
        for (int i = 0; i < this.db.size(); i++) {
            Field f;

            try {
                Company c = this.db.get(i);
                f = c.getClass().getDeclaredField("id");
                f.setAccessible(true);
                String companyId = (String) f.get(c);


                Field f2 = entity.getClass().getDeclaredField("id");
                f2.setAccessible(true);
                String entityId = (String) f2.get(entity);

                if (entityId.equals(companyId)) {
                    this.db.set(i, entity);
                    return entity;
                }

            } catch (NoSuchFieldException | IllegalAccessException e ) {
                throw new RuntimeException(e);
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
        return this.db.size();
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
