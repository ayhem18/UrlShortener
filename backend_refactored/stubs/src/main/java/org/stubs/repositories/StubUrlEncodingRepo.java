package org.stubs.repositories;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.stereotype.Repository;
import org.user.entities.AppUser;
import org.user.entities.UrlEncoding;
import org.user.repositories.UrlEncodingRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
@Repository
public class StubUrlEncodingRepo implements UrlEncodingRepository {
    
    private final List<UrlEncoding> urlEncodings = new ArrayList<>();
    
    @Override
    public List<UrlEncoding> findAll() {
        return new ArrayList<>(urlEncodings);
    }

    @Override
    public List<UrlEncoding> findAllById(Iterable<String> strings) {
        return List.of();
    }

    @Override
    public Page<UrlEncoding> findAll(Pageable pageable) {
        List<UrlEncoding> content = urlEncodings.stream()
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, urlEncodings.size());
    }
    
    @Override
    public Page<UrlEncoding> findByUser(AppUser user, Pageable pageable) {
        List<UrlEncoding> userContent = urlEncodings.stream()
                .filter(encoding -> encoding.getUser().getEmail().equals(user.getEmail()))
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
        
        long total = urlEncodings.stream()
                .filter(encoding -> encoding.getUser().getEmail().equals(user.getEmail()))
                .count();
        
        return new PageImpl<>(userContent, pageable, total);
    }

    @Override
    public List<UrlEncoding> findByUser(AppUser user) {
        return urlEncodings.stream()
                .filter(encoding -> encoding.getUser().getEmail().equals(user.getEmail()))
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<UrlEncoding> findByUserAndUrlEncodingCountGreaterThan(AppUser user, long urlEncodingCount, Pageable pageable) {
        List<UrlEncoding> filteredContent = urlEncodings.stream()
                .filter(encoding -> encoding.getUser().getEmail().equals(user.getEmail())
                        && encoding.getUrlEncodingCount() > urlEncodingCount)
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
        
        // sort the filtered content by urlEncodingTime in descending order
        filteredContent.sort(Comparator.comparing(UrlEncoding::getUrlEncodingTime).reversed());

        long total = filteredContent.size();
        
        return new PageImpl<>(filteredContent, pageable, total);
    }

    @Override
    public List<UrlEncoding> findByUserAndUrlEncodingCountGreaterThan(AppUser user, long urlEncodingCount) {
        List<UrlEncoding> filteredContent = urlEncodings.stream()
                .filter(encoding -> encoding.getUser().getEmail().equals(user.getEmail())
                        && encoding.getUrlEncodingCount() > urlEncodingCount)
                .collect(Collectors.toList());

        // sort the filtered content by urlEncodingTime in descending order
        filteredContent.sort(Comparator.comparing(UrlEncoding::getUrlEncodingTime).reversed());

        return filteredContent;
    }

    @Override
    public List<UrlEncoding> findByUserAndUrlEncodingTimeAfter(AppUser user, LocalDateTime time) {
        return urlEncodings.stream()
                .filter(encoding -> encoding.getUser().getEmail().equals(user.getEmail())
                        && encoding.getUrlEncodingTime().isAfter(time))
                .collect(Collectors.toList());
    }
    
    @Override
    public <S extends UrlEncoding> S save(S entity) {
        // Remove existing entity with same ID if present
        // For UrlEncoding, we'll use a combination of user email and encoded URL as an identifier
        urlEncodings.removeIf(e -> 
            e.getUser().getEmail().equals(entity.getUser().getEmail()) && 
            e.getUrlEncoded().equals(entity.getUrlEncoded()));
        
        urlEncodings.add(entity);
        return entity;
    }
    
    @Override
    public <S extends UrlEncoding> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        entities.forEach(entity -> {
            save(entity);
            result.add(entity);
        });
        return result;
    }
    
    @Override
    public void deleteAll() {
        urlEncodings.clear();
    }
    
    @Override
    public long count() {
        return urlEncodings.size();
    }
    
    // The remaining methods from MongoRepository interface with minimal implementations
    // These are likely not used in your tests but are required by the interface
    
    @Override
    public Optional<UrlEncoding> findById(String id) {
        // Since UrlEncoding doesn't have a clear ID field in the provided code,
        // this is a placeholder implementation
        return Optional.empty();
    }
    
    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }
    

    @Override
    public void deleteById(String id) {
        findById(id).ifPresent(urlEncodings::remove);
    }
    
    @Override
    public void delete(UrlEncoding entity) {
        urlEncodings.remove(entity);
    }
    
    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
        ids.forEach(this::deleteById);
    }
    
    @Override
    public void deleteAll(Iterable<? extends UrlEncoding> entities) {
        entities.forEach(this::delete);
    }
    
    @Override
    public List<UrlEncoding> findAll(Sort sort) {
        // Basic implementation without actual sorting
        return findAll();
    }

    @Override
    public <S extends UrlEncoding> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends UrlEncoding> S insert(S entity) {
        return null;
    }

    @Override
    public <S extends UrlEncoding> List<S> insert(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public <S extends UrlEncoding> List<S> findAll(Example<S> example) {
        return new ArrayList<>();
    }
    
    @Override
    public <S extends UrlEncoding> List<S> findAll(Example<S> example, Sort sort) {
        return new ArrayList<>();
    }
    
    @Override
    public <S extends UrlEncoding> Page<S> findAll(Example<S> example, Pageable pageable) {
        return new PageImpl<>(new ArrayList<>());
    }
    
    @Override
    public <S extends UrlEncoding> long count(Example<S> example) {
        return 0;
    }
    
    @Override
    public <S extends UrlEncoding> boolean exists(Example<S> example) {
        return false;
    }
    
    @Override
    public <S extends UrlEncoding, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

} 