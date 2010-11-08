package org.apache.isis.viewer.restful.viewer.facets;

import java.text.MessageFormat;

import nu.xom.Element;

import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnFacetFacetType extends TableColumnFacet {
    private final String pathPrefix;

    public TableColumnFacetFacetType(final String pathPrefix, final ResourceContext resourceContext) {
        super("FacetType", resourceContext);
        this.pathPrefix = pathPrefix;
    }

    @Override
    public Element doTd(final Facet facet) {
        final String facetTypeCanonicalName = facet.facetType().getCanonicalName();
        final String uri = MessageFormat.format("{0}/facet/{1}", pathPrefix, facetTypeCanonicalName);
        return new Element(xhtmlRenderer.aHref(uri, facetTypeCanonicalName, "facet", "spec", HtmlClass.FACET));
    }
}
