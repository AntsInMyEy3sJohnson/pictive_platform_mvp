package io.pictive.platform.domain.image;

import io.pictive.platform.persistence.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    public List<Image> getAll() {

        return imageRepository.findAll();

    }

}
