package org.apache.isis.viewer.restful.viewer.facets;

import nu.xom.Element;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.interactions2.DisablingInteractionAdvisor;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnFacetDisabling extends TableColumnFacet {
    public TableColumnFacetDisabling(final ResourceContext resourceContext) {
        super("Disabling", resourceContext);
    }

    @Override
    public Element doTd(final Facet facet) {
        return xhtmlRenderer.p(facet instanceof DisablingInteractionAdvisor, null);
    }
}
