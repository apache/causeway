package org.apache.isis.extensions.viewer.wicket.pdfjs.ui;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.viewer.wicket.pdfjs.metamodel.IsisModuleExtPdfjsMetaModel;
import org.apache.isis.extensions.viewer.wicket.pdfjs.ui.components.PdfJsViewerPanelComponentFactory;

@Configuration
@Import({
        // modules
        IsisModuleExtPdfjsMetaModel.class,

        // @Component's
        PdfJsViewerPanelComponentFactory.class,
})
public class IsisModuleExtPdfjsUi {
}
