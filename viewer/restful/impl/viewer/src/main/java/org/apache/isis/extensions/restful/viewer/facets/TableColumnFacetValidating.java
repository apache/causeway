package org.apache.isis.extensions.restful.viewer.facets;

import nu.xom.Element;

import org.apache.isis.extensions.restful.viewer.xom.ResourceContext;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.interactions.ValidatingInteractionAdvisor;


public final class TableColumnFacetValidating extends TableColumnFacet {
    public TableColumnFacetValidating(final ResourceContext resourceContext) {
        super("Validating", resourceContext);
    }

    @Override
    public Element doTd(final Facet facet) {
        return xhtmlRenderer.p(facet instanceof ValidatingInteractionAdvisor, null);
    }
}
