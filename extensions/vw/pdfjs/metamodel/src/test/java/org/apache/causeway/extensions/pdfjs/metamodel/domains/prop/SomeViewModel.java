package org.apache.causeway.extensions.pdfjs.metamodel.domains.prop;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.extensions.pdfjs.applib.annotations.PdfJsViewer;
import org.apache.causeway.extensions.pdfjs.applib.config.Scale;

@DomainObject(nature = Nature.VIEW_MODEL)
public class SomeViewModel {

    @Property
    @PdfJsViewer(initialScale = Scale._0_75, initialHeight = 1000, initialPageNum = 2)
    public Blob getPdf() {
        return null;
    }
}
