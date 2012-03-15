package org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery;

import javax.persistence.NamedQuery;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;


public class JpaNamedQueryFacetAnnotation extends JpaNamedQueryFacetAbstract
        implements JpaNamedQueryFacet {

    public JpaNamedQueryFacetAnnotation(final NamedQuery namedQuery,
            final FacetHolder holder) {
        super(holder);
        add(namedQuery);
    }


}
