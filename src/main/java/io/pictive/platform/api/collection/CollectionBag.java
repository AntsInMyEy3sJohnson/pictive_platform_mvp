package io.pictive.platform.api.collection;

import io.pictive.platform.domain.collection.Collection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(staticName = "of")
@Getter
public class CollectionBag {

    private final List<Collection> collections;

}
