package io.pictive.platform.api.image;

import graphql.kickstart.tools.GraphQLMutationResolver;
import io.pictive.platform.api.UuidHelper;
import io.pictive.platform.domain.images.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageMutationService implements GraphQLMutationResolver {

    private final ImageService imageService;
    private final UuidHelper uuidHelper;

    public ImageBag uploadImagesWithThumbnail(String ownerID, String collectionID, List<String> base64PayloadsWithThumbnail) {

        return ImageBag.of(Collections.emptyList());

    }

    public ImageBag uploadImages(String ownerID, String collectionID, List<String> base64Payloads) {

        return ImageBag.of(imageService.create(uuidHelper.asUuid(ownerID), uuidHelper.asUuid(collectionID), base64Payloads));

    }


}
