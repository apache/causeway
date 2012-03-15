package org.apache.isis.extensions.jpa.metamodel.facets.object.entity;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;


public abstract class JpaEntityFacetAbstract extends FacetAbstract implements
        JpaEntityFacet {

    public static Class<? extends Facet> type() {
        return JpaEntityFacet.class;
    }

    private final String name;

    public JpaEntityFacetAbstract(final String name, final FacetHolder holder) {
        super(JpaEntityFacetAbstract.type(), holder, false);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
