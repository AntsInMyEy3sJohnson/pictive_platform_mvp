package io.pictive.platform.domain.image;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TextExtractionService {

    public void extractAndAddText(List<Image> images) {

        // TODO Add text extraction
        images.forEach(image -> image.setExtractedText("Implement text extraction"));

    }
}
