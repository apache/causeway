package org.apache.isis.viewer.restful.viewer.facets;

import nu.xom.Element;

import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.interactions.ValidatingInteractionAdvisor;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnFacetValidating extends TableColumnFacet {
    public TableColumnFacetValidating(final ResourceContext resourceContext) {
        super("Validating", resourceContext);
    }

    @Override
    public Element doTd(final Facet facet) {
        return xhtmlRenderer.p(facet instanceof ValidatingInteractionAdvisor, null);
    }
}
