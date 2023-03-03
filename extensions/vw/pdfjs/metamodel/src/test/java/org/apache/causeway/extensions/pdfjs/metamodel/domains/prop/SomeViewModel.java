package org.apache.causeway.extensions.pdfjs.metamodel.domains.prop;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.extensions.pdfjs.applib.annotations.PdfJsViewer;

@DomainObject(nature = Nature.VIEW_MODEL)
public class SomeViewModel {

    @Property
    @PdfJsViewer
    public Blob getPdf() {
        return null;
    }
}
