package org.apache.isis.extensions.jpa.metamodel.facets.prop.onetoone;

import javax.persistence.FetchType;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.extensions.jpa.metamodel.facets.JpaFetchTypeFacetAbstract;


public class JpaFetchTypeFacetDerivedFromJpaOneToOneAnnotation extends
        JpaFetchTypeFacetAbstract {

    public JpaFetchTypeFacetDerivedFromJpaOneToOneAnnotation(
            final FetchType fetchType, final FacetHolder holder) {
        super(holder, fetchType);
    }

}
