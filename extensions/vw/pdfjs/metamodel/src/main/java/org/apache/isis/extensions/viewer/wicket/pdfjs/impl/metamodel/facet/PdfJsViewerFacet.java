package org.apache.isis.extensions.viewer.wicket.pdfjs.impl.metamodel.facet;

import org.wicketstuff.pdfjs.PdfJsConfig;

import org.apache.isis.metamodel.facets.MultipleValueFacet;

import org.apache.isis.extensions.viewer.wicket.pdfjs.impl.applib.spi.PdfJsViewerAdvisor;

public interface PdfJsViewerFacet extends MultipleValueFacet {

    PdfJsConfig configFor(final PdfJsViewerAdvisor.InstanceKey instanceKey);

}
