package io.pictive.platform.domain.images;


import io.pictive.platform.domain.DomainObject;
import io.pictive.platform.domain.collections.Collection;
import io.pictive.platform.domain.users.User;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Image extends DomainObject {

    public static Image withProperties(String thumbnail, String content) {

        // TODO Use timestamp on image itself rather than server-created  timestamp
        return new Image(UUID.randomUUID(), thumbnail, content, new HashSet<>(), System.currentTimeMillis());

    }

    @EqualsAndHashCode.Include
    @NonNull
    @Id
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    // TODO Is it a good idea to store the thumbnails on the server side? To optimize rendering, a thumbnail should depend on the devices screen dimensions...
    @NonNull
    @Lob
    private String thumbnail;

    @NonNull
    @Lob
    private String content;

    private String extractedText;

    @ManyToOne
    private User owner;

    @Embedded
    @ElementCollection(targetClass = ScoredLabel.class)
    @Fetch(FetchMode.JOIN)
    private List<ScoredLabel> scoredLabels;

    @NonNull
    @ManyToMany(mappedBy = "images", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Fetch(FetchMode.JOIN)
    private Set<Collection> containedInCollections;

    @NonNull
    private long creationTimestamp;

    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @RequiredArgsConstructor
    @Getter
    public static class ScoredLabel {

        @NonNull
        private String label;

        @NonNull
        private Float score;

    }

}
