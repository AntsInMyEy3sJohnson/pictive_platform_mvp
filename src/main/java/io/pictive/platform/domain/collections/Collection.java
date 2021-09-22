package io.pictive.platform.domain.collections;

import io.pictive.platform.domain.DomainObject;
import io.pictive.platform.domain.images.Image;
import io.pictive.platform.domain.users.User;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Collection extends DomainObject {

    public static Collection withProperties(String displayName, boolean defaultCollection, int pin,
                                            boolean nonOwnersCanShare, boolean nonOwnersCanWrite) {

        return new Collection(UUID.randomUUID(), defaultCollection, new HashSet<>(), new HashSet<>(),
                displayName, pin, nonOwnersCanShare, nonOwnersCanWrite, System.currentTimeMillis());

    }

    @EqualsAndHashCode.Include
    @NonNull
    @Id
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @NonNull
    private boolean defaultCollection;

    @NonNull
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Fetch(FetchMode.JOIN)
    @JoinTable(name = "contained_images",
            joinColumns = {@JoinColumn(name = "fk_collection")},
            inverseJoinColumns = {@JoinColumn(name = "fk_image")})
    private Set<Image> images;

    @NonNull
    @ManyToMany(mappedBy = "sharedCollections", cascade = CascadeType.MERGE)
    @Fetch(FetchMode.JOIN)
    private Set<User> sharedWith;

    @ManyToOne
    private User owner;

    @NonNull
    private String displayName;

    @NonNull
    private int pin;

    @NonNull
    private boolean nonOwnersCanShare;

    @NonNull
    private boolean nonOwnersCanWrite;

    @NonNull
    private long creationTimestamp;

}
