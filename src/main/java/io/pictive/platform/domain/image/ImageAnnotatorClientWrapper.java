package io.pictive.platform.domain.image;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageAnnotatorClientWrapper {

    private final ImageAnnotatorClient imageAnnotatorClient;

    public BatchAnnotateImagesResponse batchAnnotateImages(List<AnnotateImageRequest> annotateImageRequests) {

        return imageAnnotatorClient.batchAnnotateImages(annotateImageRequests);

    }

}
