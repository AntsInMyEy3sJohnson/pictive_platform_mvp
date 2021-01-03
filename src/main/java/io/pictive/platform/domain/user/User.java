package io.pictive.platform.domain.user;

import io.pictive.platform.domain.collection.Collection;
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
public class User {

    public static User withProperties(String mail) {

        return new User(mail, new HashSet<>(), new HashSet<>());

    }

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue
    private UUID id;

    @NonNull
    private String mail;

    @NonNull
    @OneToMany(mappedBy = "owner", cascade = CascadeType.PERSIST)
    private Set<Collection> ownedCollections;

    @NonNull
    @ManyToMany(cascade = CascadeType.PERSIST)
    @Fetch(FetchMode.JOIN)
    @JoinTable(name = "shared_with",
            joinColumns = {@JoinColumn(name = "fk_user")},
            inverseJoinColumns = {@JoinColumn(name = "fk_collection")})
    private Set<Collection> sharedCollections;

    @OneToOne(mappedBy = "owner", cascade = CascadeType.PERSIST)
    private Collection defaultCollection;

}
