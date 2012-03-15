package org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;


public class JpaNamedQueryFacetAbstract extends FacetAbstract implements
        JpaNamedQueryFacet {

    public static Class<? extends Facet> type() {
        return JpaNamedQueryFacet.class;
    }

    private final List<NamedQuery> namedQueries = new ArrayList<NamedQuery>();

    public JpaNamedQueryFacetAbstract(final FacetHolder holder) {
        super(JpaNamedQueryFacetAbstract.type(), holder, false);
    }

    protected void add(final javax.persistence.NamedQuery... jpaNamedQueries) {
        final ObjectSpecification objSpec = (ObjectSpecification) getFacetHolder();
        for (final javax.persistence.NamedQuery jpaNamedQuery : jpaNamedQueries) {
            namedQueries.add(new NamedQuery(jpaNamedQuery, objSpec));
        }
    }

    public List<NamedQuery> getNamedQueries() {
        return Collections.unmodifiableList(namedQueries);
    }

}
