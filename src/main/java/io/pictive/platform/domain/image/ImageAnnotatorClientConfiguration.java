package io.pictive.platform.domain.image;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
public class ImageAnnotatorClientConfiguration {

    @Bean
    @Profile("PRD")
    public ImageAnnotatorClient imageAnnotatorClientProd() throws IOException {
        return ImageAnnotatorClient.create();
    }

    @Bean
    @Profile("!PRD")
    public ImageAnnotatorClient imageAnnotatorClientNonProd() {
        return Mockito.mock(ImageAnnotatorClient.class);
    }

}
