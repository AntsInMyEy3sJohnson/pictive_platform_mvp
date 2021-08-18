package io.pictive.platform.domain.images;

import io.pictive.platform.domain.search.IndexMaintainer;
import io.pictive.platform.domain.users.User;
import io.pictive.platform.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.api.client.util.Base64;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ImageServiceUnitTest {

    @Mock
    private LabelingService labelingService;

    @Mock
    private TextExtractionService textExtractionService;

    @Mock
    private IndexMaintainer indexMaintainer;

    @Mock
    private PersistenceContext<User> userPersistenceContext;

    @Mock
    private PersistenceContext<Image> imagePersistenceContext;

    @Test
    void testImageCreation() throws IOException {

        when(userPersistenceContext.find(isA(UUID.class))).thenReturn(User
                .withProperties("dummy@dummyworld.org", "s3cret"));

        var imageService = new ImageService(labelingService,
                textExtractionService, indexMaintainer, null,
                userPersistenceContext, imagePersistenceContext, null);

        var uuid = UUID.randomUUID();
        var base64 = dummyBase64Payload();
        var decoded = Base64.decodeBase64(base64);

        var images = imageService.create(uuid, Collections.singletonList(base64));

        assertThat(images)
                .extracting(Image::getPayload)
                .isEqualTo(decoded);
        assertThat(images)
                .extracting(Image::getPreview)
                .isEqualTo(dummyBase64PayloadPreview());

    }


    private String dummyBase64Payload() throws IOException {
        return Files.readString(Paths.get("./src/test/resources/kitten_base64.txt"));
    }

    private String dummyBase64PayloadPreview() {
        return "";
    }

}
