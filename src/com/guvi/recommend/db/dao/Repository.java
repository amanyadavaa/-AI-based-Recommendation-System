package com.guvi.recommend.db.dao;

import com.guvi.recommend.exception.DatabaseOperationException;

import java.util.List;
import java.util.Optional;

/**
 * Generic Repository interface defining CRUD database operations with Generics.
 *
 * @param <T>  Entity type
 * @param <ID> Primary key identifier type
 */
public interface Repository<T, ID> {
    T save(T entity) throws DatabaseOperationException;
    Optional<T> findById(ID id) throws DatabaseOperationException;
    List<T> findAll() throws DatabaseOperationException;
    boolean deleteById(ID id) throws DatabaseOperationException;
    long count() throws DatabaseOperationException;
}
