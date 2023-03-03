package org.apache.causeway.extensions.pdfjs.metamodel.domains.mixin;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.extensions.pdfjs.applib.annotations.PdfJsViewer;
import org.apache.causeway.extensions.pdfjs.applib.config.Scale;

@Property
@PdfJsViewer(initialScale = Scale._0_75, initialHeight = 1000, initialPageNum = 2)
@RequiredArgsConstructor
public class SomeViewModel_pdf {

    private final SomeViewModel someViewModel;

    public Blob prop() {
        return null;
    }
}
