package io.pictive.platform.domain.image;


import io.pictive.platform.domain.collection.Collection;
import io.pictive.platform.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Image {

    @EqualsAndHashCode.Include
    private final UUID id;

    private final User owner;

    private final String payload;

    private final String extractedText;

    private final List<ScoredLabel> scoredLabels;

    private final Set<Collection> containedInCollections;

    @AllArgsConstructor
    @Getter
    public static class ScoredLabel {

        private final String label;

        private final float score;

    }

}
