package org.apache.isis.testing.fakedata.applib.services;

import java.util.List;

public class Lorem extends AbstractRandomValueGenerator {

    com.github.javafaker.Lorem javaFakerLorem;

    Lorem(final FakeDataService fakeDataService) {
        super(fakeDataService);
        javaFakerLorem = fakeDataService.javaFaker().lorem();
    }

    public List<String> words(int num) {
        return javaFakerLorem.words(num);
    }

    public List<String> words() {
        return javaFakerLorem.words();
    }

    public String sentence(int wordCount) {
        return javaFakerLorem.sentence(wordCount);
    }

    public String sentence() {
        return javaFakerLorem.sentence();
    }

    public String paragraph(int sentenceCount) {
        return javaFakerLorem.paragraph(sentenceCount);
    }

    public String paragraph() {
        return javaFakerLorem.paragraph();
    }

    public List<String> paragraphs(int paragraphCount) {
        return javaFakerLorem.paragraphs(paragraphCount);
    }
}
