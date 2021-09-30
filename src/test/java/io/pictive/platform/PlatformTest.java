package io.pictive.platform;

import io.pictive.platform.api.collection.CollectionBag;
import io.pictive.platform.api.collection.CollectionMutationService;
import io.pictive.platform.api.image.ImageMutationService;
import io.pictive.platform.api.user.UserBag;
import io.pictive.platform.api.user.UserMutationService;
import io.pictive.platform.api.user.UserQueryService;
import io.pictive.platform.domain.collections.Collection;
import io.pictive.platform.domain.images.Image;
import io.pictive.platform.domain.users.User;
import io.pictive.platform.testhelpers.PayloadGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ComponentScan("io.pictive.platform")
public class PlatformTest {

    @Autowired
    private UserMutationService userMutationService;

    @Autowired
    private CollectionMutationService collectionMutationService;

    @Autowired
    private ImageMutationService imageMutationService;

    @Autowired
    private UserQueryService userQueryService;

    @Test
    void testCreateImageInNonDefaultCollection() throws Throwable {

        final String userMail = "dave@example.org";
        final String userID = createUserAndGetID(userMail);
        final String nonDefaultCollectionID = createCollectionAndGetID(userID);

        final Image  image = imageMutationService.uploadImages(userID, nonDefaultCollectionID, List.of(PayloadGenerator.dummyPayload())).getImages().get(0);
        final User user = userQueryService.getUserByMail(userMail).getUsers().get(0);
        final List<Collection> userCollections = List.of(user.getSharedCollections().toArray(new Collection[]{}));

        assertThat(userCollections).hasSize(2);

        final Collection collection1 = userCollections.get(0);
        final Collection collection2 = userCollections.get(1);

        assertThat(image.getContainedInCollections()).containsExactlyInAnyOrder(collection1, collection2);
        assertThat(collection1.getImages()).containsOnly(image);
        assertThat(collection2.getImages()).containsOnly(image);

    }

    String createUserAndGetID(String mail) {

        final UserBag userBag = userMutationService.createUserWithDefaultCollection(mail,"super_awesome_password");
        return userBag.getUsers().get(0).getId().toString();

    }


    String createCollectionAndGetID(String userID) {

        final CollectionBag collectionBag = collectionMutationService.createCollection(userID, "Doggos", 1234, false, false);
        return collectionBag.getCollections().get(0).getId().toString();

    }

}
