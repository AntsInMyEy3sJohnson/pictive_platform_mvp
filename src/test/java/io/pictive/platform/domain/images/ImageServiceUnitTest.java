package io.pictive.platform.domain.images;

import io.pictive.platform.domain.collections.Collection;
import io.pictive.platform.domain.search.ImageSearcher;
import io.pictive.platform.domain.search.IndexMaintainer;
import io.pictive.platform.domain.users.User;
import io.pictive.platform.persistence.PersistenceContext;
import io.pictive.platform.testhelpers.PayloadGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
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

    @Test
    void testImageCreation() throws IOException {

        final ImageService imageService = new ImageService(labelingService, textExtractionService, indexMaintainer, imageSearcher, userPersistenceContext, imagePersistenceContext, collectionPersistenceContext);

        var user = User.withProperties("dummy@example.org", "s3cret");
        var collection = Collection.withProperties("Some default collection", true,
                1234, false, false, System.currentTimeMillis());
        user.setDefaultCollection(collection);

        when(userPersistenceContext.find(isA(UUID.class))).thenReturn(user);
        when(collectionPersistenceContext.find(isA(UUID.class))).thenReturn(collection);

        var ownerID = UUID.randomUUID();
        var collectionID = UUID.randomUUID();

        var dummyPayload = PayloadGenerator.dummyPayload();

        final List<Image> images = imageService.create(ownerID, collectionID, Collections.singletonList(dummyPayload));

        assertThat(images).hasSize(1);

        var image = images.get(0);

        assertThat(image.getPayload()).isEqualTo(dummyPayload);
        assertThat(image.getOwner()).isEqualTo(user);
        assertThat(image.getContainedInCollections()).containsOnly(collection);

    }

}
