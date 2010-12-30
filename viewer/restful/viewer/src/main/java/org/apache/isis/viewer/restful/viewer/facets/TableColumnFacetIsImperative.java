package org.apache.isis.viewer.restful.viewer.facets;

import nu.xom.Element;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.ImperativeFacetUtils;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnFacetIsImperative extends TableColumnFacet {
    public TableColumnFacetIsImperative(final String headerText, final ResourceContext resourceContext) {
        super(headerText, resourceContext);
    }

    @Override
    public Element doTd(final Facet facet) {
        return xhtmlRenderer.p(ImperativeFacetUtils.isImperativeFacet(facet), null);
    }
}
