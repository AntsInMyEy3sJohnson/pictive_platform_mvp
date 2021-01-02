package io.pictive.platform.domain.collection;

import io.pictive.platform.domain.image.Image;
import io.pictive.platform.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Collection {

    private final UUID id;

    private final User owner;

    private final boolean defaultCollection;

    private final Set<Image> images;

    private final Set<User> sharedWith;

    /* Things the user can change */
    private String displayName;

    private String internalName;

    private int pin;

    private boolean nonUsersCanShare;

    private boolean nonUsersCanWrite;

}
