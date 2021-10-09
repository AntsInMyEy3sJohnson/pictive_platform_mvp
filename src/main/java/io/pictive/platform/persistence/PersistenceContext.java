package io.pictive.platform.persistence;

import io.pictive.platform.domain.DomainObject;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class PersistenceContext<T extends DomainObject> implements PersistenceProvider<T> {

    private final JpaRepository<T, UUID> repository;

    @Override
    public boolean exists(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public T find(UUID id) {

        return repository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("No such element: " + id));

    }

    @Override
    public List<T> findAll() {

        return repository.findAll();

    }

    @Override
    public List<T> findAll(Iterable<UUID> ids) {

        return repository.findAllById(ids);

    }

    @Override
    public void persist(T t) {

        repository.save(t);

    }

    @Override
    public void persistAll(List<T> ts) {

        repository.saveAll(ts);

    }

    @Override
    public void delete(T t) {

        repository.delete(t);

    }

    @Override
    public void deleteByID(UUID id) {

        repository.deleteById(id);

    }

    @Override
    public void deleteAll(Iterable<T> ts) {

        repository.deleteAll(ts);

    }


}
