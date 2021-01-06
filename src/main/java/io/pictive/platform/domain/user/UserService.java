package io.pictive.platform.domain.user;

import io.pictive.platform.domain.collection.Collection;
import io.pictive.platform.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createWithDefaultCollection(String mail) {

        var user = User.withProperties(mail);
        var collection = Collection.withProperties("Default collection of " + mail, true, -1,
                false, false);

        user.setDefaultCollection(collection);
        user.getOwnedCollections().add(collection);
        user.getSharedCollections().add(collection);

        collection.setOwner(user);
        collection.getSharedWith().add(user);

        userRepository.save(user);

        return user;

    }

    public List<User> getAll() {

        return userRepository.findAll();

    }

    public User getByID(UUID id) {

        return userRepository.findById(id).orElseThrow(() -> new IllegalStateException("No such user: " + id.toString()));

    }

}
