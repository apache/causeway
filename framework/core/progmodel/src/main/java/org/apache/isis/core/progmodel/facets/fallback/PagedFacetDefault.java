package org.apache.isis.core.progmodel.facets.fallback;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacetAbstract;

public class PagedFacetDefault extends PagedFacetAbstract {

    public PagedFacetDefault(FacetHolder holder, int value) {
        super(holder, value);
    }
}
