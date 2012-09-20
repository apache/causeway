package org.apache.isis.core.progmodel.facets.fallback;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacetAbstract;

public class PagedFacetNone extends PagedFacetAbstract {

    public PagedFacetNone(FacetHolder holder, int value) {
        super(holder, value);
    }
}
