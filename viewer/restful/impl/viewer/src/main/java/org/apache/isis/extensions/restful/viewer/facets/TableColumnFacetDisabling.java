package org.apache.isis.extensions.restful.viewer.facets;

import nu.xom.Element;

import org.apache.isis.extensions.restful.viewer.xom.ResourceContext;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.interactions.DisablingInteractionAdvisor;


public final class TableColumnFacetDisabling extends TableColumnFacet {
    public TableColumnFacetDisabling(final ResourceContext resourceContext) {
        super("Disabling", resourceContext);
    }

    @Override
    public Element doTd(final Facet facet) {
        return xhtmlRenderer.p(facet instanceof DisablingInteractionAdvisor, null);
    }
}
