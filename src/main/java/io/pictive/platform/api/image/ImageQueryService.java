package io.pictive.platform.api.image;

import graphql.kickstart.tools.GraphQLQueryResolver;
import io.pictive.platform.api.UuidHelper;
import io.pictive.platform.domain.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageQueryService implements GraphQLQueryResolver {

    private final ImageService imageService;
    private final UuidHelper uuidHelper;

    public ImageBag getImagesForUserInCollection(String userID, String collectionID) {

        return ImageBag.of(imageService.getForUserInCollection(uuidHelper.asUuid(userID), uuidHelper.asUuid(collectionID)));

    }

    public ImageBag getImages() {

        return ImageBag.of(imageService.getAll());

    }


}
