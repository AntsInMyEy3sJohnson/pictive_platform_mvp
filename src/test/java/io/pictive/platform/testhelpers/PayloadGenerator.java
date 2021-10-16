package io.pictive.platform.testhelpers;

import io.pictive.platform.domain.images.ImageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PayloadGenerator {

    private PayloadGenerator(){}

    public static String dummyPayload() {

        return dummyThumbnailWithMarkers() + dummyBase64ContentWithMarkers();

    }

    public static String dummyBase64Content() {

        try {
            return Files.readString(Paths.get("./src/test/resources/duck_gzipped_base64.txt"));
        } catch (IOException e) {
            throw new IllegalStateException("Test setup failed:" + e);
        }

    }

    public static String dummyThumbnail() {

        return "my_awesome_thumbnail";

    }

    private static String dummyBase64ContentWithMarkers() {

        return ImageService.CONTENT_START_MARKER + dummyBase64Content() + ImageService.CONTENT_END_MARKER;

    }

    private static String dummyThumbnailWithMarkers() {

        return ImageService.THUMBNAIL_START_MARKER + dummyThumbnail() + ImageService.THUMBNAIL_END_MARKER;

    }


}
