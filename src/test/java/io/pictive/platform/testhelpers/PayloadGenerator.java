package io.pictive.platform.testhelpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PayloadGenerator {

    private PayloadGenerator(){}

    public static String dummyPayload() throws IOException {

        return Files.readString(Paths.get("./src/test/resources/duck_gzipped_base64.txt"));

    }

    public static String dummyPayloadWithThumbnail() throws IOException {

        return dummyThumbnail() + dummyPayload();

    }

    public static String dummyThumbnail() {

        return "THUMBNAIL_START:my_awesome_thumbnail:THUMBNAIL_END";

    }


}
