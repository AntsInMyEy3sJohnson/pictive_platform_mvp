package io.pictive.platform.domain.collection;

import io.pictive.platform.persistence.CollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;

    public List<Collection> getAll() {

        return collectionRepository.findAll();

    }

}
