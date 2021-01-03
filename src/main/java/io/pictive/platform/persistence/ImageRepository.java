package io.pictive.platform.persistence;

import io.pictive.platform.domain.image.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {
}
