package demoapp.dom._infra.resources;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.value.Markup;

import lombok.val;


@Service
@Named("demo.MarkupReaderService")
public class MarkupReaderService {

    public Markup readFor(Class<?> aClass) {
        val markupResourceName = String.format("%s.html", aClass.getSimpleName());
        val html = resourceReaderService.readResource(aClass, markupResourceName);
        return new Markup(html);
    }

    public Markup readFor(Class<?> aClass, final String member) {
        val markupResourceName = String.format("%s-%s.html", aClass.getSimpleName(), member);
        val html = resourceReaderService.readResource(aClass, markupResourceName);
        return new Markup(html);
    }

    @Inject
    ResourceReaderService resourceReaderService;

}
