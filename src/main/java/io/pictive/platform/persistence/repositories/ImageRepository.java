package io.pictive.platform.persistence.repositories;

import io.pictive.platform.domain.image.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ImageRepository extends JpaRepository<Image, UUID> {
}
