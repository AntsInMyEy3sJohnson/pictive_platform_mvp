package io.pictive.platform.domain.images;

import io.pictive.platform.domain.collections.Collection;
import io.pictive.platform.domain.search.ImageSearcher;
import io.pictive.platform.domain.search.IndexMaintainer;
import io.pictive.platform.domain.users.User;
import io.pictive.platform.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ImageService {

    public static final String THUMBNAIL_START_MARKER = "THUMBNAIL_START:";
    public static final String THUMBNAIL_END_MARKER = ":THUMBNAIL_END";
    public static final String CONTENT_START_MARKER = "CONTENT_START:";
    public static final String CONTENT_END_MARKER = ":CONTENT_END";

    private final LabelingService labelingService;
    private final TextExtractionService textExtractionService;
    private final IndexMaintainer indexMaintainer;
    private final ImageSearcher imageSearcher;

    private final PersistenceContext<User> userPersistenceContext;
    private final PersistenceContext<Image> imagePersistenceContext;
    private final PersistenceContext<Collection> collectionPersistenceContext;

    public Image getByID(UUID id) {

        return imagePersistenceContext.find(id);

    }

    public List<Image> search(UUID ownerID, List<UUID> collectionIDs, List<String> labels, String text, String searchMode) {

        var owner = userPersistenceContext.find(ownerID);

        var collections = collectionPersistenceContext.findAll(collectionIDs);

        if (collections.stream().anyMatch(collection -> !collection.getSourcedBy().contains(owner))) {
            throw new IllegalStateException(String.format("Unable to perform search: Given list of collections contains at least one collection user '%s' does not have access to.", ownerID));
        }

        var identifiedImageIDs = imageSearcher.identifyImageMatches(collections, labels, text, searchMode);
        return imagePersistenceContext.findAll(identifiedImageIDs);

    }

    public List<Image> create(UUID ownerID, UUID collectionID, List<String> base64Payloads) {

        var owner = userPersistenceContext.find(ownerID);
        // MUST NOT query these directly from the database, as tempting as it might be given I already have the IDs... once
        // I have the User object, it will carry all nested objects (like collections), and querying them from the database
        // will deliver objects the same in terms of the ID, but NOT the same in terms of object reference, leading to
        // an IllegalStateException in Hibernate ("Multiple representations of the same entity being merged") when attempting
        // to persist back the object tree
        var collection = owner.getSourcedCollections().stream().filter(c -> c.getId().equals(collectionID)).findAny().get();
        var defaultCollection = owner.getSourcedCollections().stream().filter(Collection::isDefaultCollection).findAny().get();

        var images = base64Payloads.stream()
                .map(payload -> Image.withProperties(thumbnailFromPayload(payload), contentFromPayload(payload)))
                .peek(image -> setImageToOwnerReference(image, owner))
                .peek(image -> setImageToCollectionReference(image, collection))
                .peek(image -> setImageToCollectionReference(image, defaultCollection))
                .collect(Collectors.toList());
        labelingService.labelImages(images);
        textExtractionService.extractAndAddText(images);

        indexMaintainer.createOrUpdateCollectionIndexWithImages(owner.getDefaultCollection().getId(), images);

        imagePersistenceContext.persistAll(images);

        return imagePersistenceContext.findAll(images.stream().map(Image::getId).collect(Collectors.toList()));

    }

    public List<Image> getForUserInCollection(UUID userID, UUID collectionID) {

        var user = userPersistenceContext.find(userID);

        var collection = collectionPersistenceContext.find(collectionID);

        if (!user.getSourcedCollections().contains(collection)) {
            throw new IllegalStateException(String.format("Unable to retrieve images from collection: Collection '%s' was not shared with user '%s'.", collectionID, userID));
        }

        return new ArrayList<>(collection.getImages());

    }

    public List<Image> getAll() {

        return imagePersistenceContext.findAll();

    }

    private String thumbnailFromPayload(String payload) {

        return extractFromMarkers(payload, THUMBNAIL_START_MARKER, THUMBNAIL_END_MARKER);

    }

    private String contentFromPayload(String payload) {

        return extractFromMarkers(payload, CONTENT_START_MARKER, CONTENT_END_MARKER);

    }

    private String extractFromMarkers(String payload, String startMarker, String endMarker) {

        var intermediate = payload.substring(payload.indexOf(startMarker), payload.indexOf(endMarker));
        return intermediate.substring(intermediate.indexOf(":") + 1);

    }



    private void setImageToOwnerReference(Image image, User owner) {

        image.setOwner(owner);
        owner.getOwnedImages().add(image);

    }

    private void setImageToCollectionReference(Image image, Collection collection) {

        image.getContainedInCollections().add(collection);
        collection.getImages().add(image);

    }


}
