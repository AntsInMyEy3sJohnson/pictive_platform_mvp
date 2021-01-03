package io.pictive.platform.domain.image;


import io.pictive.platform.domain.collection.Collection;
import io.pictive.platform.domain.user.User;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Image {

    public static Image withProperties(String payload) {

        return new Image(UUID.randomUUID(), payload, List.of(new ScoredLabel("kitten", 1.0f)), new HashSet<>());

    }

    @EqualsAndHashCode.Include
    @NonNull
    @Id
    private UUID id;

    @NonNull
    private String payload;

    private String extractedText;

    @NonNull
    @Embedded
    @ElementCollection(targetClass = ScoredLabel.class)
    @Fetch(FetchMode.SUBSELECT)
    private List<ScoredLabel> scoredLabels;

    @NonNull
    @ManyToMany(mappedBy = "images")
    @Fetch(FetchMode.JOIN)
    private Set<Collection> containedInCollections;

    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @RequiredArgsConstructor
    @Getter
    public static class ScoredLabel {

        @NonNull
        private String label;

        @NonNull
        private float score;

    }

}
