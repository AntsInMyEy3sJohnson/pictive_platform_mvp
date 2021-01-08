package io.pictive.platform.domain.search;

import io.pictive.platform.domain.images.Image;
import lombok.extern.log4j.Log4j2;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@Log4j2
public class IndexMaintainer {

    public void createOrUpdateCollectionIndexWithImages(UUID collectionID, List<Image> images) {

        Directory directory = null;
        IndexWriter indexWriter = null;

        var exceptionOccurred = false;
        try {
            directory = new NIOFSDirectory(Paths.get("./lucene/" + collectionID));
            var directoryWriterConfig = new IndexWriterConfig(new StandardAnalyzer());
            indexWriter = new IndexWriter(directory, directoryWriterConfig);

            for (Image image : images) {
                var document = new Document();

                document.add(new TextField("id", image.getId().toString(), Field.Store.YES));
                document.add(new TextField("extractedText", image.getExtractedText(), Field.Store.NO));
                // TODO Make use of label scores
                image.getScoredLabels().forEach(scoredLabel -> document.add(new TextField("labels", scoredLabel.getLabel(), Field.Store.NO)));

                indexWriter.addDocument(document);
            }

        } catch (IOException e) {
            log.error("Unable to create or update document index: " + e.getMessage());
            exceptionOccurred = true;
        } finally {
            try {
                if (Objects.nonNull(indexWriter)) {
                    indexWriter.close();
                }
                if (Objects.nonNull(directory)) {
                    directory.close();
                }
            } catch (IOException e) {
                log.error("Unable to dispose of previously acquired index resources: " + e.getMessage());
                exceptionOccurred = true;
            }
        }

        if(exceptionOccurred) {
            throw new IllegalStateException("Unable to perform indexing operation.");
        }

    }

}
