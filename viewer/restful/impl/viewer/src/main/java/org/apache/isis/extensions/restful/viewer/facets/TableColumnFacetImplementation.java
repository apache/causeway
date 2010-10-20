package org.apache.isis.extensions.restful.viewer.facets;

import nu.xom.Element;

import org.apache.isis.extensions.restful.viewer.xom.ResourceContext;
import org.apache.isis.metamodel.facets.Facet;


public final class TableColumnFacetImplementation extends TableColumnFacet {

    public TableColumnFacetImplementation(final ResourceContext resourceContext) {
        super("Implementation", resourceContext);
    }

    @Override
    public Element doTd(final Facet facet) {
        return xhtmlRenderer.p(facet.getClass().getCanonicalName(), null);
    }
}
