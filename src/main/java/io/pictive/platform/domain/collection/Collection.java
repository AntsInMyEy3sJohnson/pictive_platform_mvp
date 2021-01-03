package io.pictive.platform.domain.collection;

import io.pictive.platform.domain.image.Image;
import io.pictive.platform.domain.user.User;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Collection {

    public static Collection withProperties(String displayName) {

        return new Collection(UUID.randomUUID(), true, new HashSet<>(), new HashSet<>(), displayName);

    }

    @EqualsAndHashCode.Include
    @NonNull
    @Id
    private UUID id;

    @NonNull
    private boolean defaultCollection;

    @NonNull
    @ManyToMany(cascade = CascadeType.PERSIST)
    @Fetch(FetchMode.JOIN)
    @JoinTable(name = "contained_images",
            joinColumns = {@JoinColumn(name = "fk_collection")},
            inverseJoinColumns = {@JoinColumn(name = "fk_image")})
    private Set<Image> images;

    @NonNull
    @ManyToMany(mappedBy = "sharedCollections")
    @Fetch(FetchMode.JOIN)
    private Set<User> sharedWith;

    @ManyToOne
    private User owner;

    @NonNull
    private String displayName;

    private int pin;

    private boolean nonOwnersCanShare;

    private boolean nonOwnersCanWrite;

}
