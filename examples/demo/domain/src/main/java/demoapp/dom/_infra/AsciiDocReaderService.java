package demoapp.dom._infra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

import lombok.val;


@Service
@Named("demoapp.AsciiDocReaderService")
public class AsciiDocReaderService {

    public AsciiDoc readFor(Object anObject) {
        return readFor(anObject.getClass());
    }

    public AsciiDoc readFor(Object anObject, final String member) {
        return readFor(anObject.getClass(), member);
    }

    public AsciiDoc readFor(Class<?> aClass) {
        val adocResourceName = String.format("%s.adoc", aClass.getSimpleName());
        val asciiDoc = readAsciiDoc(aClass, adocResourceName);
        return AsciiDoc.valueOfAdoc(asciiDoc);
    }

    public AsciiDoc readFor(Class<?> aClass, final String member) {
        val adocResourceName = String.format("%s-%s.adoc", aClass.getSimpleName(), member);
        val asciiDoc = readAsciiDoc(aClass, adocResourceName);
        return AsciiDoc.valueOfAdoc(asciiDoc);
    }

    private String readAsciiDoc(Class<?> aClass, String adocResourceName) {
        val adocResource = aClass.getResourceAsStream(adocResourceName);
        if(adocResource==null) {
            return String.format("AsciiDoc resource '%s' not found.", adocResourceName);
        }
        try {
            return read(adocResource);
        } catch (IOException e) {
            return String.format("Failed to read from adoc resource '%s': '%s': ", adocResourceName, e.getMessage());
        }
    }

    /**
     * Read the given {@code input} into a String, while also pre-processing placeholders.
     *
     * @param input
     * @return
     * @throws IOException
     */
    private String read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines()
                    .map(markupVariableResolverService::resolveVariables)
                    .collect(Collectors.joining("\n"));
        }
    }

    @Inject
    MarkupVariableResolverService markupVariableResolverService;

}
