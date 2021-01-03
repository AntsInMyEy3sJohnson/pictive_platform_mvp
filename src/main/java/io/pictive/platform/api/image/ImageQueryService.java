package io.pictive.platform.api.image;

import graphql.kickstart.tools.GraphQLQueryResolver;
import io.pictive.platform.domain.image.Image;
import io.pictive.platform.domain.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageQueryService implements GraphQLQueryResolver {

    private final ImageService imageService;

    public ImageBag getImages() {

        return ImageBag.of(imageService.getAll());

    }


}
