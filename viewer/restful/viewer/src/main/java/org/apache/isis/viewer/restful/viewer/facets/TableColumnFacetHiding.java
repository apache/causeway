package org.apache.isis.viewer.restful.viewer.facets;

import nu.xom.Element;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.interactions2.HidingInteractionAdvisor;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnFacetHiding extends TableColumnFacet {
    public TableColumnFacetHiding(final ResourceContext resourceContext) {
        super("Hiding", resourceContext);
    }

    @Override
    public Element doTd(final Facet facet) {
        return xhtmlRenderer.p(facet instanceof HidingInteractionAdvisor, null);
    }
}
