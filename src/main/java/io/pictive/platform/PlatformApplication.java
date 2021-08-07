package io.pictive.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * To prevent GCV dependencies from attempting to authenticate when a non-production Spring profile is active,
 * add the following environment variables:
 * <p>
 * SPRING_PROFILES_ACTIVE=local;SPRING_CLOUD_GCP_VISION_ENABLED=false;SPRING_CLOUD_GCP_CORE_ENABLED=false
 */
@SpringBootApplication
public class PlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }

}
