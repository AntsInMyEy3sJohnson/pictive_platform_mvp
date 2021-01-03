package io.pictive.platform.api.image;

import io.pictive.platform.domain.image.Image;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(staticName = "of")
@Getter
public class ImageBag {

    private final List<Image> images;

}
