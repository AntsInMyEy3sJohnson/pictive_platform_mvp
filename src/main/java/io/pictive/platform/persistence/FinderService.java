package io.pictive.platform.persistence;

import io.pictive.platform.domain.DomainObject;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

// FIXME Dedicated layer for repository access?
// It does not make sense for the service layer to reference this class if they reference the underlying repository anyway...
@Service
public class FinderService<T extends DomainObject> {

    public T findOrThrowWithMessage(UUID id, Function<UUID, Optional<T>> retrievalFunction, String message) {

        return retrievalFunction.apply(id).orElseThrow(() -> new IllegalStateException(message));

    }

}
