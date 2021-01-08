package io.pictive.platform.api.image;

import graphql.kickstart.tools.GraphQLMutationResolver;
import io.pictive.platform.api.UuidHelper;
import io.pictive.platform.domain.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageMutationService implements GraphQLMutationResolver {

    private final ImageService imageService;
    private final UuidHelper uuidHelper;

    public ImageBag uploadImages(String ownerID, List<String> base64Payloads) {

        return ImageBag.of(imageService.create(uuidHelper.asUuid(ownerID), base64Payloads));

    }


}