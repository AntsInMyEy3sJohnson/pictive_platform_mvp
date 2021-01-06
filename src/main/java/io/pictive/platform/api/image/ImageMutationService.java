package io.pictive.platform.api.image;

import graphql.kickstart.tools.GraphQLMutationResolver;
import io.pictive.platform.domain.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageMutationService implements GraphQLMutationResolver {

    private final ImageService imageService;

    public ImageBag createImages(String ownerID, List<String> base64Payloads) {

        return ImageBag.of(imageService.create(UUID.fromString(ownerID), base64Payloads));

    }


}
