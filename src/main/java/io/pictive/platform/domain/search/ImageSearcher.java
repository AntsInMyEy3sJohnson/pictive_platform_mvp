package io.pictive.platform.domain.search;

import io.pictive.platform.domain.collections.Collection;
import lombok.extern.log4j.Log4j2;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.NIOFSDirectory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Log4j2
public class ImageSearcher {

    private static final String LUCENE_BASE = "./lucene";

    public List<UUID> identifyImageMatches(List<Collection> collections, List<String> labels, String text, String searchMode) {

        var typedSearchMode = SearchMode.valueOf(searchMode);

        validateInput(typedSearchMode, labels, text);

        var foundImageIDs = new LinkedList<UUID>();
        for (Collection collection : collections) {
            foundImageIDs.addAll(identifyMatchesInCollection(collection, labels, text, typedSearchMode));
        }

        return foundImageIDs.stream()
                .distinct()
                .collect(Collectors.toList());

    }

    private List<UUID> identifyMatchesInCollection(Collection collection, List<String> labels, String text, SearchMode searchMode) {

        DirectoryReader directoryReader = null;
        try {
            var directory = new NIOFSDirectory(Paths.get(LUCENE_BASE + "/" + collection.getId()));
            directoryReader = DirectoryReader.open(directory);
            var indexSearcher = new IndexSearcher(directoryReader);

            Query query = null;
            if (searchMode == SearchMode.LABELS_AND_TEXT) {
                query = assembleQueryForLabelsAndTextSearch(labels, text);
            } else if (searchMode == SearchMode.TEXT_ONLY) {
                query = assembleQueryForTextOnlySearch(text);
            } else {
                query = assembleQueryForLabelsOnlySearch(labels);
            }

            var topDocs = indexSearcher.search(query, Integer.MAX_VALUE);
            var documents = new ArrayList<Document>();

            for (ScoreDoc scoreDoc : List.of(topDocs.scoreDocs)) {
                documents.add(indexSearcher.doc(scoreDoc.doc));
            }

            return documents.stream()
                    .map(document -> document.get("id"))
                    .map(UUID::fromString)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Unable to perform search in directory: " + e.getMessage());
        } finally {
            if (Objects.nonNull(directoryReader)) {
                try {
                    directoryReader.close();
                } catch (IOException e) {
                    log.error("Unable to dispose of resources used to search directory: " + e.getMessage());
                }
            }
        }

        throw new IllegalStateException("Unable to perform search on directory for collection: " + collection.getId());

    }

    private void validateInput(SearchMode searchMode, List<String> labels, String text) {

        if (searchMode == SearchMode.LABELS_AND_TEXT && (labelsEmpty(labels) || textEmpty(text))) {
            throw new IllegalArgumentException("Unable to perform search: Instructed to do label-and-text search, but either text or labels were empty.");
        }

        if (searchMode == SearchMode.TEXT_ONLY && textEmpty(text)) {
            throw new IllegalArgumentException("Unable to perform search: Instructed to text-based search, but given text was empty.");
        }

        if (searchMode == SearchMode.LABELS_ONLY && labelsEmpty(labels)) {
            throw new IllegalArgumentException("Unable to perform search: Instructed to do label search, but given list of labels was empty.");
        }

    }

    private Query assembleQueryForLabelsAndTextSearch(List<String> labels, String text) {

        var booleanQueryBuilder = new BooleanQuery.Builder();

        booleanQueryBuilder.add(assembleQueryForLabelsOnlySearch(labels), BooleanClause.Occur.SHOULD);
        booleanQueryBuilder.add(assembleQueryForTextOnlySearch(text), BooleanClause.Occur.SHOULD);

        return booleanQueryBuilder.build();

    }

    private Query assembleQueryForTextOnlySearch(String text) {

        return new FuzzyQuery(new Term("extractedText", text));

    }

    private Query assembleQueryForLabelsOnlySearch(List<String> labels) {

        var booleanQueryBuilder = new BooleanQuery.Builder();

        for (String label : labels) {
            var term = new Term("labels", label);
            var termQuery = new TermQuery(term);
            booleanQueryBuilder.add(termQuery, BooleanClause.Occur.SHOULD);
        }

        return booleanQueryBuilder.build();

    }

    private boolean textEmpty(String text) {

        return StringUtils.isEmpty(text);

    }

    private boolean labelsEmpty(List<String> labels) {

        return labels == null || labels.isEmpty();

    }


}
