package org.apache.isis.extensions.jpa.metamodel.facets;

import javax.persistence.FetchType;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;


public abstract class JpaFetchTypeFacetAbstract extends FacetAbstract implements
        JpaFetchTypeFacet {

    public static Class<? extends Facet> type() {
        return JpaFetchTypeFacet.class;
    }

    private final FetchType fetchType;

    public JpaFetchTypeFacetAbstract(final FacetHolder holder,
            final FetchType fetchType) {
        super(JpaFetchTypeFacetAbstract.type(), holder, false);
        this.fetchType = fetchType;
    }

    public FetchType getFetchType() {
        return fetchType;
    }

}
