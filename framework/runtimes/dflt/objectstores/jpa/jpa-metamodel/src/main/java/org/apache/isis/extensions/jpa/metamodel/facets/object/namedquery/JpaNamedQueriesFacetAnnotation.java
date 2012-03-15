package org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery;

import javax.persistence.NamedQuery;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;


public class JpaNamedQueriesFacetAnnotation extends JpaNamedQueryFacetAbstract
        implements JpaNamedQueryFacet {

    public JpaNamedQueriesFacetAnnotation(final NamedQuery[] namedQueries,
            final FacetHolder holder) {
        super(holder);
        add(namedQueries);
    }


}
