package demoapp.dom._infra.resources;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.springframework.stereotype.Service;

import lombok.val;


@Service
@Named("demo.AsciiDocConverterService")
public class AsciiDocConverterService {

    private static ThreadLocal<Class<?>> context = new ThreadLocal<>();


    private Asciidoctor asciidoctor;
    private Options options;

    private Asciidoctor getAsciidoctor() {
        if(asciidoctor == null) {
            asciidoctor = Asciidoctor.Factory.create();
            asciidoctor.javaExtensionRegistry().includeProcessor(new LocalIncludeProcessor());
        }
        return asciidoctor;
    }

     class LocalIncludeProcessor extends IncludeProcessor {

        @Override
        public boolean handles(String target) {
            return true;
        }

        @Override
        public void process(Document document, PreprocessorReader reader, String target, Map<String, Object> attributes) {
            Class<?> contextClass = context.get();
            val content = resourceReaderService.readResource(contextClass, target, attributes);
            reader.push_include(content, target, target, 1, attributes);
        }
    }

    private Options getOptions() {
        if (options == null) {
            options = OptionsBuilder.options()
                    .safe(SafeMode.UNSAFE)
                    .toFile(false)
                    .attributes(AttributesBuilder.attributes()
                            .sourceHighlighter("prism")
                            .icons("font")
                            .get())
                    .get();
        }
        return options;
    }


    String adocToHtml(Class<?> contextClass, String adoc) {
        try {
            context.set(contextClass);
            return getAsciidoctor().convert(adoc, getOptions());
        } finally {
            context.remove();
        }
    }


    @Inject
    ResourceReaderService resourceReaderService;

}
