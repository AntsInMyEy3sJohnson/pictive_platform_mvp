package io.pictive.platform.domain.image;

import io.pictive.platform.domain.collection.Collection;
import io.pictive.platform.domain.user.User;
import io.pictive.platform.persistence.ImageRepository;
import io.pictive.platform.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageLabelingService imageLabelingService;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    public List<Image> create(UUID ownerID, List<String> base64Payloads) {

        var owner = userRepository.findById(ownerID).orElseThrow(() -> new IllegalStateException("No such user: " + ownerID.toString()));

        var images = base64Payloads.stream()
                .map(Image::withProperties)
                .peek(image -> setImageToOwnerReference(image, owner))
                .peek(image -> setImageToDefaultCollectionReference(image, owner.getDefaultCollection()))
                .collect(Collectors.toList());
        imageLabelingService.labelImages(images);

        imageRepository.saveAll(images);

        return images;

    }

    public List<Image> getAll() {

        return imageRepository.findAll();

    }

    private void setImageToOwnerReference(Image image, User owner) {

        image.setOwner(owner);
        owner.getOwnedImages().add(image);

    }

    private void setImageToDefaultCollectionReference(Image image, Collection defaultCollection) {

        image.getContainedInCollections().add(defaultCollection);
        defaultCollection.getImages().add(image);

    }


}
