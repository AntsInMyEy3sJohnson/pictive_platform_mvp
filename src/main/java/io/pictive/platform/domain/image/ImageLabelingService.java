package io.pictive.platform.domain.image;

import com.google.api.client.util.Base64;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.ImageContext;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageLabelingService {

    private final ImageAnnotatorClientWrapper imageAnnotatorClientWrapper;
    private final Environment environment;

    public void labelImages(List<Image> images) {

        if (!isProductionProfileActive()) {
            doDummyLabeling(images);
        } else {
            doProductionLabeling(images);
        }

    }

    private void doProductionLabeling(List<Image> images) {
        List<AnnotateImageRequest> annotateImageRequests = images.stream()
                .map(image -> Optional.ofNullable(assembleAnnotateRequest(image.getPayload())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        /* The batch annotation response object will contain the responses in the order of the requests (i. e., the order
         * of requests is preserved in the response) */
        var batchAnnotationResponse = imageAnnotatorClientWrapper.batchAnnotateImages(annotateImageRequests);
        var responses = batchAnnotationResponse.getResponsesList();

        if (responses.size() != images.size()) {
            throw new IllegalStateException(String.format("Encountered %d images for labeling, but received %d responses " +
                    "from labeling API.", images.size(), responses.size()));
        }

        for (int i = 0; i < responses.size(); i++) {
            var response = responses.get(i);
            List<Image.ScoredLabel> scoredLabels = response.getLabelAnnotationsList().stream()
                    .map(entityAnnotation -> new Image.ScoredLabel(entityAnnotation.getDescription(), entityAnnotation.getScore()))
                    .collect(Collectors.toList());
            images.get(i).setScoredLabels(scoredLabels);
        }

    }

    private void doDummyLabeling(List<Image> images) {
        images.forEach(image -> image.setScoredLabels(List.of(new Image.ScoredLabel("kitten", 1.0f),
                new Image.ScoredLabel("awww", 0.99f))));
    }

    private AnnotateImageRequest assembleAnnotateRequest(String base64Payload) {

        if (base64Payload.startsWith("data:")) {
            base64Payload = base64Payload.substring(base64Payload.indexOf(",") + 1);
        }

        var byteArrayResource = new ByteArrayResource(Base64.decodeBase64(base64Payload));

        ByteString imageBytes = null;
        try {
            imageBytes = ByteString.readFrom(byteArrayResource.getInputStream());
            com.google.cloud.vision.v1.Image image = com.google.cloud.vision.v1.Image.newBuilder().setContent(imageBytes).build();

            Feature labelDetectionFeature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();

            return AnnotateImageRequest.newBuilder()
                    .addFeatures(labelDetectionFeature)
                    .setImageContext(ImageContext.getDefaultInstance())
                    .setImage(image)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    private boolean isProductionProfileActive() {

        return Arrays.asList(environment.getActiveProfiles()).contains("PRD");

    }

}
