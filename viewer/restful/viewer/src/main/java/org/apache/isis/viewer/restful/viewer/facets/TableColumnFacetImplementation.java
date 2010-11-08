package org.apache.isis.viewer.restful.viewer.facets;

import nu.xom.Element;

import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnFacetImplementation extends TableColumnFacet {

    public TableColumnFacetImplementation(final ResourceContext resourceContext) {
        super("Implementation", resourceContext);
    }

    @Override
    public Element doTd(final Facet facet) {
        return xhtmlRenderer.p(facet.getClass().getCanonicalName(), null);
    }
}
