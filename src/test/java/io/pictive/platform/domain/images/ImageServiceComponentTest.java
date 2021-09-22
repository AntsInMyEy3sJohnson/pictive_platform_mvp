package io.pictive.platform.domain.images;

import io.pictive.platform.domain.collections.Collection;
import io.pictive.platform.domain.search.ImageSearcher;
import io.pictive.platform.domain.search.IndexMaintainer;
import io.pictive.platform.domain.users.User;
import io.pictive.platform.persistence.PersistenceContext;
import io.pictive.platform.testhelpers.PayloadGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ImageService.class,
        LabelingService.class,
        TextExtractionService.class,
        IndexMaintainer.class,
        ImageSearcher.class,
        ImageAnnotatorClientConfiguration.class,
        ImageAnnotatorClientWrapper.class}
)
public class ImageServiceComponentTest {

    @Autowired
    private ImageService imageService;

    @SuppressWarnings("unused")
    @SpyBean
    private LabelingService labelingService;

    @SuppressWarnings("unused")
    @SpyBean
    private TextExtractionService textExtractionService;

    @SuppressWarnings("unused")
    @SpyBean
    private IndexMaintainer indexMaintainer;

    @SuppressWarnings("unused")
    @SpyBean
    private ImageSearcher imageSearcher;

    @SuppressWarnings("unused")
    @MockBean
    private PersistenceContext<User> userPersistenceContext;

    @SuppressWarnings("unused")
    @MockBean
    private PersistenceContext<Image> imagePersistenceContext;

    @SuppressWarnings("unused")
    @MockBean
    private PersistenceContext<Collection> collectionPersistenceContext;

    @SuppressWarnings("unchecked")
    @Test
    void testImageCreation() throws IOException {

        var user = User.withProperties("dummy@example.org", "s3cret");
        var collection = Collection.withProperties("Some default collection", true,
                1234, false, false);
        user.setDefaultCollection(collection);

        when(userPersistenceContext.find(isA(UUID.class))).thenReturn(user);
        when(collectionPersistenceContext.find(isA(UUID.class))).thenReturn(collection);

        var ownerID = UUID.randomUUID();
        var collectionID = UUID.randomUUID();

        var dummyPayload = PayloadGenerator.dummyPayload();

        final List<Image> createdImages = imageService.create(ownerID, collectionID, Collections.singletonList(dummyPayload));

        assertThat(createdImages)
                .allMatch(image -> image.getPayload().equals(dummyPayload))
                .allMatch(image -> image.getOwner().equals(user))
                .satisfies(images -> {
                    assertThat(images).hasSize(1);
                    assertThat(images).extracting(Image::getContainedInCollections).containsOnly(Set.of(collection));
                });
        assertThat(createdImages.get(0).getScoredLabels())
                .hasSize(2);
        assertThat(createdImages.get(0).getScoredLabels())
                .allMatch(scoredLabel -> scoredLabel.getLabel().equals("kitten") || scoredLabel.getLabel().equals("awww"));

    }


}
