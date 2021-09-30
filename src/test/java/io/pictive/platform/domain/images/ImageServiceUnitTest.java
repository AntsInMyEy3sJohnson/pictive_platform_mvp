package io.pictive.platform.domain.images;

import io.pictive.platform.domain.collections.Collection;
import io.pictive.platform.domain.search.ImageSearcher;
import io.pictive.platform.domain.search.IndexMaintainer;
import io.pictive.platform.domain.users.User;
import io.pictive.platform.persistence.PersistenceContext;
import io.pictive.platform.testhelpers.PayloadGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageServiceUnitTest {

    @Mock
    private LabelingService labelingService;

    @Mock
    private TextExtractionService textExtractionService;

    @Mock
    private IndexMaintainer indexMaintainer;

    @Mock
    private ImageSearcher imageSearcher;

    @Mock
    private PersistenceContext<User> userPersistenceContext;

    @Mock
    private PersistenceContext<Image> imagePersistenceContext;

    @Mock
    private PersistenceContext<Collection> collectionPersistenceContext;

    @Captor
    private ArgumentCaptor<List<Image>> imageArgumentCaptor;

    @Test
    void testCreateImageInDefaultCollection() throws IOException {

        final ImageService imageService = createImageService();

        var user = createUser();
        var defaultCollection = createCollection("Default collection", true);
        user.setDefaultCollection(defaultCollection);

        when(userPersistenceContext.find(isA(UUID.class))).thenReturn(user);
        when(collectionPersistenceContext.find(isA(UUID.class))).thenReturn(defaultCollection);

        imageService.create(user.getId(), defaultCollection.getId(), Collections.singletonList(PayloadGenerator.dummyPayload()));
        verify(imagePersistenceContext).persistAll(imageArgumentCaptor.capture());

        final List<Image> images = imageArgumentCaptor.getValue();

        var image = images.get(0);

        assertThat(image.getContainedInCollections()).hasSize(1).containsOnly(defaultCollection);
        assertThat(defaultCollection.getImages()).hasSize(1).containsOnly(image);

    }

    @Test
    void testCreateImageInNonDefaultCollection() throws IOException {

        final ImageService imageService = createImageService();

        var user = createUser();
        var defaultCollection = createCollection("Default collection", true);
        var doggoCollection = createCollection("Doggos", false);
        user.setDefaultCollection(defaultCollection);

        when(userPersistenceContext.find(isA(UUID.class))).thenReturn(user);
        when(collectionPersistenceContext.find(eq(defaultCollection.getId()))).thenReturn(defaultCollection);
        when(collectionPersistenceContext.find(eq(doggoCollection.getId()))).thenReturn(doggoCollection);

        var dummyPayload = PayloadGenerator.dummyPayload();

        imageService.create(user.getId(), doggoCollection.getId(), Collections.singletonList(dummyPayload));
        verify(imagePersistenceContext).persistAll(imageArgumentCaptor.capture());

        final List<Image> images = imageArgumentCaptor.getValue();

        assertThat(images).hasSize(1);

        var image = images.get(0);

        assertThat(image.getPayload()).isEqualTo(dummyPayload);
        assertThat(image.getOwner()).isEqualTo(user);
        assertThat(image.getContainedInCollections()).containsExactlyInAnyOrder(doggoCollection, defaultCollection);

        assertThat(defaultCollection.getImages()).containsOnly(image);
        assertThat(doggoCollection.getImages()).containsOnly(image);

    }

    private Collection createCollection(String name, boolean defaultCollection) {
        return Collection.withProperties(name, defaultCollection, 1234, false, false);
    }

    private User createUser() {
        return User.withProperties("dummy@example.org", "s3cret");
    }

    private ImageService createImageService() {

        return new ImageService(labelingService, textExtractionService, indexMaintainer, imageSearcher,
                userPersistenceContext, imagePersistenceContext, collectionPersistenceContext);

    }

}
