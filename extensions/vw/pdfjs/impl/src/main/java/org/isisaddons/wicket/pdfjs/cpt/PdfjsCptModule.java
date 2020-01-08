package org.isisaddons.wicket.pdfjs.cpt;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.isisaddons.wicket.pdfjs.cpt.ui.PdfViewerPanelComponentFactory;

@Configuration
@Import({
        // @Component's
        PdfViewerPanelComponentFactory.class,
        PdfJsMetaModelPlugin.class
})
public class PdfjsCptModule {
}
