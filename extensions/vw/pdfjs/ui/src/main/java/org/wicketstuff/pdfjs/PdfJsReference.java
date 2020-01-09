package org.wicketstuff.pdfjs;

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.ajax.WicketAjaxJQueryResourceReference;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.JQueryPluginResourceReference;

public class PdfJsReference extends JQueryPluginResourceReference {

    public static final PdfJsReference INSTANCE = new PdfJsReference();

    private PdfJsReference() {
        super(PdfJsReference.class, "res/pdf.js");
    }

    @Override
    public List<HeaderItem> getDependencies() {
        final List<HeaderItem> dependencies = super.getDependencies();
        ResourceReference wicketEventReference;
        if (Application.exists()) {
            wicketEventReference = Application.get()
                    .getJavaScriptLibrarySettings().getJQueryReference();
        } else {
            wicketEventReference = WicketAjaxJQueryResourceReference.get();
        }
        dependencies.add(JavaScriptHeaderItem.forReference(wicketEventReference));
        return dependencies;
    }
}
