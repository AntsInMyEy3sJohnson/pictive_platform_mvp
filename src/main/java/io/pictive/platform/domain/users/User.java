package io.pictive.platform.domain.users;

import io.pictive.platform.domain.DomainObject;
import io.pictive.platform.domain.collections.Collection;
import io.pictive.platform.domain.images.Image;
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
public class User extends DomainObject {

    public static User withProperties(String mail) {

        return new User(UUID.randomUUID(), mail, new HashSet<>(), new HashSet<>(), new HashSet<>());

    }

    @EqualsAndHashCode.Include
    @NonNull
    @Id
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @NonNull
    private String mail;

    @NonNull
    @OneToMany(mappedBy = "owner", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Fetch(FetchMode.JOIN)
    private Set<Collection> ownedCollections;

    @NonNull
    @OneToMany(mappedBy = "owner", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Fetch(FetchMode.JOIN)
    private Set<Image> ownedImages;

    @NonNull
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Fetch(FetchMode.JOIN)
    @JoinTable(name = "shared_with",
            joinColumns = {@JoinColumn(name = "fk_user")},
            inverseJoinColumns = {@JoinColumn(name = "fk_collection")})
    private Set<Collection> sharedCollections;

    @OneToOne(mappedBy = "owner", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Collection defaultCollection;

}
