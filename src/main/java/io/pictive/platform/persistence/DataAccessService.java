package io.pictive.platform.persistence;

import io.pictive.platform.domain.collection.Collection;
import io.pictive.platform.domain.image.Image;
import io.pictive.platform.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * TODO Try to generify this...
 */
@Service
@RequiredArgsConstructor
public class DataAccessService {

    private final CollectionRepository collectionRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    public User findUser(UUID id) {

        return userRepository.findById(id).orElseThrow(exceptionWithMessage("No such user: " + id));

    }

    public Collection findCollection(UUID id) {

        return collectionRepository.findById(id).orElseThrow(exceptionWithMessage("No such collection: " + id));

    }

    public void saveImages(List<Image> images) {

        imageRepository.saveAll(images);

    }

    public void saveCollection(Collection collection) {

        collectionRepository.save(collection);

    }

    public void saveUser(User user) {

        userRepository.save(user);

    }

    public void saveUsers(List<User> users) {

        userRepository.saveAll(users);

    }

    public List<Collection> getAllCollections() {

        return collectionRepository.findAll();

    }

    public List<Image> getAllImages() {

        return imageRepository.findAll();

    }

    public List<User> getAllUsers() {

        return userRepository.findAll();

    }

    private Supplier<IllegalStateException> exceptionWithMessage(String message) {

        return () -> new IllegalStateException(message);

    }


}
