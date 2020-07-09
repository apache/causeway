package demoapp.dom._infra.resources;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

import lombok.val;


@Service
@Named("demo.AsciiDocReaderService")
public class AsciiDocReaderService {

    public AsciiDoc readFor(Object anObject) {
        return readFor(anObject.getClass());
    }

    public AsciiDoc readFor(Object anObject, final String member) {
        return readFor(anObject.getClass(), member);
    }

    public AsciiDoc readFor(Class<?> aClass) {
        val adocResourceName = String.format("%s.adoc", aClass.getSimpleName());
        val asciiDoc = resourceReaderService.readResource(aClass, adocResourceName);
        return AsciiDoc.valueOfHtml(asciiDocConverterService.adocToHtml(aClass, asciiDoc));
    }

    public AsciiDoc readFor(Class<?> aClass, final String member) {
        val adocResourceName = String.format("%s-%s.%s", aClass.getSimpleName(), member, "adoc");
        val asciiDoc = resourceReaderService.readResource(aClass, adocResourceName);
        return AsciiDoc.valueOfHtml(asciiDocConverterService.adocToHtml(aClass, asciiDoc));
    }


    @Inject
    AsciiDocConverterService asciiDocConverterService;

    @Inject
    ResourceReaderService resourceReaderService;

}
