package io.pictive.platform.domain.image;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageLabelingService {

    public void labelImages(List<Image> images) {

        // TODO Implement image labeling
        images.forEach(image -> image.setScoredLabels(List.of(new Image.ScoredLabel("Implement me", 1.0f))));

    }

}
