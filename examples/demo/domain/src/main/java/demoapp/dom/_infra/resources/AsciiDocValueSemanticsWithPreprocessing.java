package demoapp.dom._infra.resources;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;
import org.apache.isis.valuetypes.asciidoc.metamodel.semantics.AsciiDocValueSemantics;

@Component
@Named("demo.AsciiDocValueSemantics")
@Qualifier("demo-adoc-pre-processor")
public class AsciiDocValueSemanticsWithPreprocessing
extends AsciiDocValueSemantics {

    @Inject AsciiDocConverterService asciiDocConverterService;

    @Override
    public String simpleTextRepresentation(final ValueSemanticsProvider.Context context, final AsciiDoc adoc) {

        return render(adoc, plainAdoc->
            asciiDocConverterService
            .adocToHtml(
                    context.getIdentifier().getLogicalType().getCorrespondingClass(),
                    plainAdoc.getAdoc()));
    }

}
