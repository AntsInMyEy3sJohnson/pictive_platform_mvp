package io.pictive.platform.domain.user;

import io.pictive.platform.domain.collection.Collection;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @EqualsAndHashCode.Include
    private final UUID id;

    private final String mail;

    private final Set<Collection> sharedCollections;

}
