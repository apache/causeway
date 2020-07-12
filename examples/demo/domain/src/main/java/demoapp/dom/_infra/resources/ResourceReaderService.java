package demoapp.dom._infra.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.valuetypes.markdown.applib.value.Converter;
import org.apache.isis.valuetypes.markdown.applib.value.Markdown;

import lombok.val;


@Service
@Named("demo.ResourceReaderService")
public class ResourceReaderService {

    public String readResource(Class<?> aClass, String resourceName) {
        return readResource(aClass, resourceName, Collections.emptyMap());
    }
    public String readResource(Class<?> aClass, String resourceName, Map<String, Object> attributes) {
        val resourceStream = aClass.getResourceAsStream(resourceName);
        if(resourceStream==null) {
            return String.format("Resource '%s' not found.", resourceName);
        }
        try {
            return read(resourceStream, attributes);
        } catch (IOException e) {
            return String.format("Failed to read from resource '%s': '%s': ", resourceName, e.getMessage());
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
        return read(input, Collections.emptyMap());
    }

    /**
     * Read the given {@code input} into a String, while also pre-processing placeholders.
     *
     * @param input
     * @return
     * @throws IOException
     */
    private String read(InputStream input, Map<String, Object> attributes) throws IOException {
        val in = new InputStreamReader(input);
        val tagHandler = new TagHandler(attributes);
        try (val bufferReader = new BufferedReader(in)) {
            return bufferReader.lines()
                    .map(tagHandler::handle)
                    .filter(Objects::nonNull)
                    .map(markupVariableResolverService::resolveVariables)
                    .collect(Collectors.joining("\n"));
        }
    }

    @Inject
    MarkupVariableResolverService markupVariableResolverService;

}
