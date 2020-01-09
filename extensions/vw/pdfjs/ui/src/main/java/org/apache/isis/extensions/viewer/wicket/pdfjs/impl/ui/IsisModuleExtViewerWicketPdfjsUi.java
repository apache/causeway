package org.apache.isis.extensions.viewer.wicket.pdfjs.impl.ui;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.viewer.wicket.pdfjs.impl.metamodel.IsisModuleExtViewerWicketPdfjsMetaModel;
import org.apache.isis.extensions.viewer.wicket.pdfjs.impl.ui.components.PdfJsViewerPanelComponentFactory;

@Configuration
@Import({
        // modules
        IsisModuleExtViewerWicketPdfjsMetaModel.class,

        // @Component's
        PdfJsViewerPanelComponentFactory.class,
})
public class IsisModuleExtViewerWicketPdfjsUi {
}
