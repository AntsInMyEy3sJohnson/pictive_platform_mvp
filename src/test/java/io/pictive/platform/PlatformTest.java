package io.pictive.platform;

import io.pictive.platform.api.collection.CollectionBag;
import io.pictive.platform.api.collection.CollectionMutationService;
import io.pictive.platform.api.collection.CollectionQueryService;
import io.pictive.platform.api.image.ImageBag;
import io.pictive.platform.api.image.ImageMutationService;
import io.pictive.platform.api.image.ImageQueryService;
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

import java.io.IOException;
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
    private ImageQueryService imageQueryService;

    @Autowired
    private UserQueryService userQueryService;

    @Autowired
    private CollectionQueryService collectionQueryService;

    @Test
    void testGetImageByID() {

        String mail = "dave@spaceodyssey.org";
        final User user = createUserAndGet(mail);
        final String userID = user.getId().toString();

        final Collection defaultCollection = createCollectionAndGet(userID);

        imageMutationService.uploadImages(userID, defaultCollection.getId().toString(), List.of(PayloadGenerator.dummyPayload()));

        final ImageBag allImages = imageQueryService.getImages();

        assertThat(allImages.getImages()).hasSize(1);

        final String imageID = allImages.getImages().get(0).getId().toString();

        final ImageBag imageByID = imageQueryService.getImageByID(imageID);

        assertThat(imageByID.getImages()).hasSize(1);

        assertThat(imageByID.getImages()).containsExactly(allImages.getImages().toArray(new Image[0]));

    }

    @Test
    void testDeleteCollectionWithDeletingImages() {

        String mail = "donna@example.org";
        final User userBeforeDeletion = createUserAndGet(mail);
        final String userID = userBeforeDeletion.getId().toString();

        final Collection collection = createCollectionAndGet(userID);
        final String collectionID = collection.getId().toString();

        imageMutationService.uploadImages(userID, collectionID, List.of(PayloadGenerator.dummyPayload()));

        assertThat(collectionMutationService.deleteCollection(collectionID, true).getCollections()).isEmpty();
        assertThat(collectionQueryService.getCollectionByID(collectionID).getCollections()).isEmpty();

        final Collection userDefaultCollection = collectionQueryService.getCollectionByID(userBeforeDeletion.getDefaultCollection().getId().toString()).getCollections().get(0);

        assertThat(userDefaultCollection.getImages()).isEmpty();

    }

    @Test
    void testDeleteCollectionWithoutDeletingImages() {

        String mail = "steve@example.org";
        final User userBeforeDeletion = createUserAndGet(mail);
        final String userID = userBeforeDeletion.getId().toString();

        final Collection collection = createCollectionAndGet(userID);
        final String collectionID = collection.getId().toString();

        imageMutationService.uploadImages(userID, collectionID, List.of(PayloadGenerator.dummyPayload()));

        assertThat(collectionMutationService.deleteCollection(collectionID, false).getCollections()).isEmpty();
        assertThat(collectionQueryService.getCollectionByID(collectionID).getCollections()).isEmpty();

        final User userAfterDeletion = userQueryService.getUserByMail(mail).getUsers().get(0);

        assertThat(userAfterDeletion.getOwnedCollections()).doesNotContain(collection);
        assertThat(userAfterDeletion.getSharedCollections()).doesNotContain(collection);

        final Collection userDefaultCollection = collectionQueryService.getCollectionByID(userBeforeDeletion.getDefaultCollection().getId().toString()).getCollections().get(0);

        // We have deleted the collection without deleting its images, so the default collection must still contain the image
        assertThat(userDefaultCollection.getImages()).hasSize(1);

        assertThat(collectionQueryService.getCollections().getCollections()).hasSize(1);

    }

    @Test
    void testCreateImageInNonDefaultCollection() {

        final String userMail = "dave@example.org";
        final String userID = createUserAndGetID(userMail);
        final String nonDefaultCollectionID = createCollectionAndGetID(userID);

        final Image image = imageMutationService.uploadImages(userID, nonDefaultCollectionID, List.of(PayloadGenerator.dummyPayload())).getImages().get(0);
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

        return createUserAndGet(mail).getId().toString();

    }

    User createUserAndGet(String mail) {

        final UserBag userBag = userMutationService.createUserWithDefaultCollection(mail,"super_awesome_password");
        return userBag.getUsers().get(0);

    }


    String createCollectionAndGetID(String userID) {

        return createCollectionAndGet(userID).getId().toString();

    }

    Collection createCollectionAndGet(String userID) {

        final CollectionBag collectionBag = collectionMutationService.createCollection(userID, "Doggos", 1234, false, false);
        return collectionBag.getCollections().get(0);

    }

}
