package io.pictive.platform.domain.user;

import io.pictive.platform.domain.collection.Collection;
import io.pictive.platform.persistence.PersistenceAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {

    private final PersistenceAccessService<User> userPersistenceAccessService;

    public User createWithDefaultCollection(String mail) {

        var user = User.withProperties(mail);
        var collection = Collection.withProperties("Default collection of " + mail, true, -1,
                false, false);

        user.setDefaultCollection(collection);
        user.getOwnedCollections().add(collection);
        user.getSharedCollections().add(collection);

        collection.setOwner(user);
        collection.getSharedWith().add(user);

        userPersistenceAccessService.persist(user);

        return user;

    }

    public List<User> getAll() {

        return userPersistenceAccessService.findAll();

    }

}
