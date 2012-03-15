package org.apache.isis.extensions.jpa.metamodel.facets;

import javax.persistence.FetchType;

import org.apache.isis.core.metamodel.facetapi.Facet;

/**
 * Captures a {@link FetchType}; factored out from other annotations.
 */
public interface JpaFetchTypeFacet extends Facet {

    FetchType getFetchType();
}
