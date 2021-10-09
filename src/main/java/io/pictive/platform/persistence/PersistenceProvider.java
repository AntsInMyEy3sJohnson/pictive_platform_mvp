package io.pictive.platform.persistence;

import java.util.List;
import java.util.UUID;

public interface PersistenceProvider<T> {

    boolean exists(UUID id);

    T find(UUID id);

    List<T> findAll();

    List<T> findAll(Iterable<UUID> ids);

    void persist(T t);

    void persistAll(List<T> ts);

    void delete(T t);

    void deleteByID(UUID id);

    void deleteAll(Iterable<T> ts);

}
