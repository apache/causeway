package org.apache.isis.extensions.viewer.wicket.pdfjs.ui.components;

import java.util.List;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.resource.JQueryPluginResourceReference;
import org.wicketstuff.pdfjs.WicketStuffPdfJsReference;

class PdfJsViewerReference extends JQueryPluginResourceReference {

    public PdfJsViewerReference() {
        super(PdfJsViewerPanel.class, "PdfJsViewerPanel.js");
    }

    @Override
    public List<HeaderItem> getDependencies() {
        final List<HeaderItem> dependencies = super.getDependencies();
        dependencies.add(JavaScriptHeaderItem.forReference(WicketStuffPdfJsReference.INSTANCE));
        return dependencies;
    }
}
