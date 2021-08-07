package io.pictive.platform.domain.users;

import io.pictive.platform.domain.collections.Collection;
import io.pictive.platform.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {

    private final PersistenceContext<User> userPersistenceContext;

    public User createWithDefaultCollection(String mail, String password) {

        var user = User.withProperties(mail, password);
        var collection = Collection.withProperties("Default collection of " + mail, true, -1,
                false, false);

        user.setDefaultCollection(collection);
        user.getOwnedCollections().add(collection);
        user.getSharedCollections().add(collection);

        collection.setOwner(user);
        collection.getSharedWith().add(user);

        userPersistenceContext.persist(user);

        return user;

    }

    public List<User> getAll() {

        return userPersistenceContext.findAll();

    }

}
