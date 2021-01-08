package io.pictive.platform.domain.image;

import io.pictive.platform.domain.collection.Collection;
import io.pictive.platform.domain.user.User;
import io.pictive.platform.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.SimpleFSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final String LUCENE_BASE = "./lucene";

    private final LabelingService labelingService;
    private final TextExtractionService textExtractionService;

    private final PersistenceContext<User> userPersistenceContext;
    private final PersistenceContext<Image> imagePersistenceContext;
    private final PersistenceContext<Collection> collectionPersistenceContext;

    public List<Image> search(UUID ownerID, List<UUID> collectionIDs, List<String> labels, String extractedText, String searchMode) {

        var owner = userPersistenceContext.find(ownerID);

        var collections = collectionPersistenceContext.findAll(collectionIDs);

        if (collections.stream().anyMatch(collection -> !collection.getSharedWith().contains(owner))) {
            throw new IllegalStateException(String.format("Unable to perform search: Given list of collections contains at least one collection user '%s' does not have access to.", ownerID));
        }

        if(SearchMode.valueOf(searchMode) == SearchMode.LABELS_ONLY && (labels == null || labels.isEmpty())) {
            throw new IllegalArgumentException("Unable to perform search: Instructed to do label search, but given list of labels was empty.");
        }

        var foundImages = new ArrayList<Image>();
        for (Collection collection : collections) {
            try {
                // For now, implement only search on labels
                var directory = new SimpleFSDirectory(Paths.get(LUCENE_BASE + "/" + collection.getId()));
                var directoryReader = DirectoryReader.open(directory);
                var directorySearcher = new IndexSearcher(directoryReader);

                var booleanQueryBuilder = new BooleanQuery.Builder();

                for (String label : labels) {
                    var term = new Term("labels", label);
                    var termQuery = new TermQuery(term);
                    booleanQueryBuilder.add(termQuery, BooleanClause.Occur.SHOULD);
                }

                var booleanQuery = booleanQueryBuilder.build();

                var topDocs = directorySearcher.search(booleanQuery, Integer.MAX_VALUE);
                var documents = new ArrayList<Document>();

                for (ScoreDoc scoreDoc : List.of(topDocs.scoreDocs)) {
                    documents.add(directorySearcher.doc(scoreDoc.doc));
                }

                var ids = documents.stream()
                        .map(document -> document.get("id"))
                        .map(UUID::fromString)
                        .collect(Collectors.toList());
                foundImages.addAll(imagePersistenceContext.findAll(ids));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return foundImages;

    }

    public List<Image> create(UUID ownerID, List<String> base64Payloads) {

        var owner = userPersistenceContext.find(ownerID);

        var images = base64Payloads.stream()
                .map(Image::withProperties)
                .peek(image -> setImageToOwnerReference(image, owner))
                .peek(image -> setImageToDefaultCollectionReference(image, owner.getDefaultCollection()))
                .collect(Collectors.toList());
        labelingService.labelImages(images);
        textExtractionService.extractAndAddText(images);

        try {
            addImagesToSearchIndexInCollectionDocument(owner.getDefaultCollection().getId(), images);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imagePersistenceContext.persistAll(images);

        return images;

    }

    public List<Image> getForUserInCollection(UUID userID, UUID collectionID) {

        var user = userPersistenceContext.find(userID);

        var collection = collectionPersistenceContext.find(collectionID);

        if (!user.getSharedCollections().contains(collection)) {
            throw new IllegalStateException(String.format("Unable to retrieve images from collection: Collection '%s' was not shared with user '%s'.", collectionID, userID));
        }

        return new ArrayList<>(collection.getImages());

    }

    public List<Image> getAll() {

        return imagePersistenceContext.findAll();

    }

    private void addImagesToSearchIndexInCollectionDocument(UUID collectionID, List<Image> images) throws IOException {

        var directory = new SimpleFSDirectory(Paths.get("./lucene/" + collectionID));
        var directoryWriterConfig = new IndexWriterConfig(new StandardAnalyzer());
        var directoryWriter = new IndexWriter(directory, directoryWriterConfig);

        for (Image image : images) {
            var document = new Document();

            document.add(new TextField("id", image.getId().toString(), Field.Store.YES));
            document.add(new TextField("extractedText", image.getExtractedText(), Field.Store.NO));
            image.getScoredLabels().forEach(scoredLabel -> document.add(new TextField("labels", scoredLabel.getLabel(), Field.Store.NO)));

            directoryWriter.addDocument(document);
        }

        directoryWriter.close();
        directory.close();

    }

    private void setImageToOwnerReference(Image image, User owner) {

        image.setOwner(owner);
        owner.getOwnedImages().add(image);

    }

    private void setImageToDefaultCollectionReference(Image image, Collection defaultCollection) {

        image.getContainedInCollections().add(defaultCollection);
        defaultCollection.getImages().add(image);

    }

    public static enum SearchMode {
        LABELS_ONLY,
        LABELS_AND_TEXT,
        TEXT_ONLY
    }


}
