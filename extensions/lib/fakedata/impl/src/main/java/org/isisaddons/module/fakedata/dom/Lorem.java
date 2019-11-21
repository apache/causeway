package org.isisaddons.module.fakedata.dom;

import java.util.List;
import org.apache.isis.applib.annotation.Programmatic;

public class Lorem extends AbstractRandomValueGenerator {

    com.github.javafaker.Lorem javaFakerLorem;

    Lorem(final FakeDataService fakeDataService) {
        super(fakeDataService);
        javaFakerLorem = new com.github.javafaker.Lorem(fakeDataService.fakeValuesService, fakeDataService.randomService);
    }

    @Programmatic
    public List<String> words(int num) {
        return javaFakerLorem.words(num);
    }

    @Programmatic
    public List<String> words() {
        return javaFakerLorem.words();
    }

    @Programmatic
    public String sentence(int wordCount) {
        return javaFakerLorem.sentence(wordCount);
    }

    @Programmatic
    public String sentence() {
        return javaFakerLorem.sentence();
    }

    @Programmatic
    public String paragraph(int sentenceCount) {
        return javaFakerLorem.paragraph(sentenceCount);
    }

    @Programmatic
    public String paragraph() {
        return javaFakerLorem.paragraph();
    }

    @Programmatic
    public List<String> paragraphs(int paragraphCount) {
        return javaFakerLorem.paragraphs(paragraphCount);
    }
}
