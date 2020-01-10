package org.apache.isis.extensions.viewer.wicket.pdfjs.metamodel.facet;

import org.apache.isis.extensions.viewer.wicket.pdfjs.applib.config.PdfJsConfig;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import org.apache.isis.extensions.viewer.wicket.pdfjs.applib.spi.PdfJsViewerAdvisor;


public abstract class PdfJsViewerFacetAbstract extends FacetAbstract implements PdfJsViewerFacet {

    private final PdfJsConfig config;

    public PdfJsViewerFacetAbstract(
            final PdfJsConfig config,
            final FacetHolder holder) {
        super(type(), holder);

        this.config = config;
    }

    public PdfJsConfig configFor(final PdfJsViewerAdvisor.InstanceKey instanceKey) {
        return config;
    }

    public static Class<? extends Facet> type() {
        return PdfJsViewerFacet.class;
    }

}
