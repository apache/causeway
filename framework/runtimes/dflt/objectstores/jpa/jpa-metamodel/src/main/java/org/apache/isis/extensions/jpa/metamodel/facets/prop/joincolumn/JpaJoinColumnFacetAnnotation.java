package org.apache.isis.extensions.jpa.metamodel.facets.prop.joincolumn;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;


public class JpaJoinColumnFacetAnnotation extends FacetAbstract implements
        JpaJoinColumnFacet {

    public static Class<? extends Facet> type() {
        return JpaJoinColumnFacet.class;
    }

    private final String name;

    public JpaJoinColumnFacetAnnotation(final String name,
            final FacetHolder holder) {
        super(JpaJoinColumnFacetAnnotation.type(), holder, false);
        this.name = name;
    }

    public String name() {
        return name;
    }

}
