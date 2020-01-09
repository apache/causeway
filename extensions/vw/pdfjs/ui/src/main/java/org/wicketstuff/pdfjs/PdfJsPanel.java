package org.wicketstuff.pdfjs;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.lang.Args;

import org.apache.isis.extensions.viewer.wicket.pdfjs.applib.config.PdfJsConfig;

/**
 * A panel for rendering PDF documents inline in the page
 */
public class PdfJsPanel extends Panel {

    private final PdfJsConfig config;

    /**
     * Constructor.
     *
     * @param id The component id
     */
    public PdfJsPanel(String id, PdfJsConfig config) {
        super(id);

        this.config = Args.notNull(config, "config");

        final WebComponent pdfJsCanvas = new WebComponent("pdfJsCanvas");
        pdfJsCanvas.setOutputMarkupId(true);
        config.withCanvasId(pdfJsCanvas.getMarkupId());
        add(pdfJsCanvas);
    }

    public final PdfJsConfig getConfig() {
        return config;
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(JavaScriptHeaderItem.forReference(PdfJsReference.INSTANCE));
        renderWicketStuffPdfJs(response);
    }

    protected void renderWicketStuffPdfJs(final IHeaderResponse response) {
        config.withWorkerUrl(createPdfJsWorkerUrl());
        response.render(JavaScriptHeaderItem.forReference(WicketStuffPdfJsReference.INSTANCE));
        response.render(OnDomReadyHeaderItem.forScript(String.format("WicketStuff.PDFJS.init(%s)", config.toJsonString())));
    }

    protected String createPdfJsWorkerUrl() {
        final CharSequence _pdfJsUrl = urlFor(PdfJsReference.INSTANCE, null);
        final Url pdfJsUrl = Url.parse(_pdfJsUrl);
        final Url pdfJsWorkerUrl = Url.parse("./pdf.worker.js");
        pdfJsUrl.resolveRelative(pdfJsWorkerUrl);
        return pdfJsUrl.toString();
    }

}
