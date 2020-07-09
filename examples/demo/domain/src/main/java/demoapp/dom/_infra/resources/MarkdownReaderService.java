package demoapp.dom._infra.resources;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.valuetypes.markdown.applib.value.Converter;
import org.apache.isis.valuetypes.markdown.applib.value.Markdown;

import lombok.val;


@Service
@Named("demo.MarkdownReaderService")
public class MarkdownReaderService {

    public Markdown readFor(Object anObject) {
        return readFor(anObject.getClass());
    }

    public Markdown readFor(Object anObject, final String member) {
        return readFor(anObject.getClass(), member);
    }

    public Markdown readFor(Class<?> aClass) {
        val adocResourceName = String.format("%s.md", aClass.getSimpleName());
        val markdown = resourceReaderService.readResource(aClass, adocResourceName);
        return Markdown.valueOfHtml(Converter.mdToHtml(markdown));
    }

    public Markdown readFor(Class<?> aClass, final String member) {
        val markdownResourceName = String.format("%s-%s.md", aClass.getSimpleName(), member);
        val markdown = resourceReaderService.readResource(aClass, markdownResourceName);
        return Markdown.valueOfHtml(Converter.mdToHtml(markdown));
    }


    @Inject
    ResourceReaderService resourceReaderService;

}
