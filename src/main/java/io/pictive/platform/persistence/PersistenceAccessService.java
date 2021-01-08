package io.pictive.platform.persistence;

import io.pictive.platform.domain.DomainObject;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class PersistenceAccessService<T extends DomainObject> implements PersistenceProvider<T> {

    private final JpaRepository<T, UUID> repository;

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
    public void persist(T t) {

        repository.save(t);

    }

    @Override
    public void persistAll(List<T> ts) {

        repository.saveAll(ts);

    }

    private Supplier<IllegalStateException> exceptionWithMessage(String message) {

        return () -> new IllegalStateException(message);

    }

    private String withIllegalArgument() {

        return "Unable to perform persistence operation: No repository registered for generic type: "
                + retrieveGenericType(getClass()).getType().getTypeName();

    }

    private ResolvableType retrieveGenericType(Class<?> clasz) {

        return ResolvableType.forClass(clasz).getGeneric(0);

    }

}
