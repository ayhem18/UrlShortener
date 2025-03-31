package org.stubs.repositories;

import org.access.Role;
import org.company.entities.Company;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;
import org.tokens.entities.AppToken;
import org.tokens.entities.AppToken.TokenState;
import org.tokens.repositories.TokenRepository;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@SuppressWarnings({"unused", "null", "NullableProblems", "ConstantConditions"})
public class StubTokenRepo implements TokenRepository {
    private final List<AppToken> db;
    private final StubCompanyRepo companyRepo;

    public StubTokenRepo(StubCompanyRepo companyRepo) {
        this.db = new ArrayList<>();
        this.companyRepo = companyRepo;
    }


    public List<AppToken> getDb() {
        return db;
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
                        token.getRole().equals(role)
                )
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

    @Override
    public long count() { return this.db.size(); }
    
    @Override
    public void deleteById(String id) {
        this.db.removeIf(token -> token.getTokenId().equals(id));
    }
    
    @Override
    public void delete(AppToken entity) {
        this.db.remove(entity);
    }

    @Override
    public <S extends AppToken> List<S> saveAll(Iterable<S> entities) {
        List<S> saved = new ArrayList<>();
        for (S entity : entities) {
            saved.add(this.save(entity));
        }
        return saved;
    }


    @Override
    public List<AppToken> findAll() { return new ArrayList<>(this.db); }

    // Implement the countByCompanyAndRole method
    @Override
    public long countByCompanyAndRole(Company company, Role role) {
        return this.db.stream()
                .filter(token -> token.getCompany() != null && 
                        token.getCompany().getId().equals(company.getId()) && 
                        token.getRole() != null && 
                        token.getRole().equals(role)
                )
                .count();
    }


    @Override
    public List<AppToken> findByCompanyAndRoleIn(Company company, List<Role> roles) {
        return this.db.stream()
        .filter(token -> token.getCompany() != null && 
                token.getCompany().getId().equals(company.getId()) && 
                token.getRole() != null && 
                roles.contains(token.getRole())
        )
        .collect(Collectors.toList());        
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<AppToken> innerSort(List<AppToken> list, Sort sort) {
        // use reflection to access all the fields in the Token class
        Field[] fields = AppToken.class.getDeclaredFields();
        
        List<Field> sortingFields = new ArrayList<>();
        List<Boolean> isAscending = new ArrayList<>();

        for (Field field : fields) {
            field.setAccessible(true);
            Sort.Order order = sort.getOrderFor(field.getName());

            if (order != null) {
                sortingFields.add(field);
                isAscending.add(order.isAscending());
            }
        }

        list.sort((a, b) -> {

            for (int i = 0; i < sortingFields.size(); i++) {
                Field field = sortingFields.get(i);
                boolean as = isAscending.get(i);
                
                try {
                    Comparable aComp = (Comparable) field.get(a);
                    Comparable bComp = (Comparable) field.get(b);

                    if (aComp.compareTo(bComp) > 0) {
                        // at this point we know that a > b
                        // if the order is ascending, then we return 1 which signals that a is bigger than b
                        // a will be moved further
                        // if the order is descending, return -1 to have a before b
                        return as ? 1 : -1;
                    }
                    else if (aComp.compareTo(bComp) < 0) {
                        // at this point, we know b > a
                        // if the order is ascending, then we return 1 so that "a" will be before "b"
                        // and -1 otherwise
                        return as ? -1: 1;
                    }

                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            return 0;
        });

        return list;
    }


    @Override
    public List<AppToken> findByCompanyAndRoleIn(Company company, List<Role> roles, Sort sort) {
        List<AppToken> res = this.findByCompanyAndRoleIn(company, roles);
        return this.innerSort(res, sort);
    }

    @Override
    public List<AppToken> findByCompanyAndRole(Company company, Role role, Sort sort) {
        List<AppToken> res = this.findByCompanyAndRole(company, role);
        return this.innerSort(res, sort);
    }


    // Stub implementations for other required methods
    @Override
    public List<AppToken> findAll(Sort sort) { return List.of(); }


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
    public List<AppToken> findAllById(Iterable<String> ids) { return List.of(); }
    

    @Override
    public void deleteAllById(Iterable<? extends String> ids) {}
    @Override
    public void deleteAll(Iterable<? extends AppToken> entities) {this.db.clear();}
    @Override
    public void deleteAll() { this.db.clear(); }

    @Override
    public Page<AppToken> findAll(Pageable pageable) { return null; }


} 