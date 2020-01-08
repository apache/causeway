package org.wicketstuff.pdfjs;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.resource.JQueryPluginResourceReference;

import java.util.List;

public class WicketStuffPdfJsReference extends JQueryPluginResourceReference {

    public static final WicketStuffPdfJsReference INSTANCE = new WicketStuffPdfJsReference();

    private WicketStuffPdfJsReference() {
        super(WicketStuffPdfJsReference.class, "res/wicketstuff-pdf.js");
    }

    @Override
    public List<HeaderItem> getDependencies() {
        final List<HeaderItem> dependencies = super.getDependencies();
        dependencies.add(JavaScriptHeaderItem.forReference(PdfJsReference.INSTANCE));
        return dependencies;
    }
}
