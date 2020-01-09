package org.apache.isis.extensions.viewer.wicket.pdfjs.impl.metamodel.facet;

import org.apache.isis.extensions.viewer.wicket.pdfjs.applib.config.PdfJsConfig;

import org.apache.isis.metamodel.facets.MultipleValueFacet;

import org.apache.isis.extensions.viewer.wicket.pdfjs.applib.spi.PdfJsViewerAdvisor;

public interface PdfJsViewerFacet extends MultipleValueFacet {

    PdfJsConfig configFor(final PdfJsViewerAdvisor.InstanceKey instanceKey);

}
