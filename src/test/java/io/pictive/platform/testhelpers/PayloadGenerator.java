package io.pictive.platform.testhelpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PayloadGenerator {

    private PayloadGenerator(){}

    public static String dummyPayload() throws IOException {

        return Files.readString(Paths.get("./src/test/resources/duck_gzipped_base64.txt"));

    }


}
