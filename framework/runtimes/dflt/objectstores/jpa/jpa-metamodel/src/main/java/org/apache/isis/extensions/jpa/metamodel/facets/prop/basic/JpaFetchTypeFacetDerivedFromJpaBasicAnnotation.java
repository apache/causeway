package org.apache.isis.extensions.jpa.metamodel.facets.prop.basic;

import javax.persistence.FetchType;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.extensions.jpa.metamodel.facets.JpaFetchTypeFacetAbstract;


public class JpaFetchTypeFacetDerivedFromJpaBasicAnnotation extends
        JpaFetchTypeFacetAbstract {

    public JpaFetchTypeFacetDerivedFromJpaBasicAnnotation(
            final FacetHolder holder, final FetchType fetchType) {
        super(holder, fetchType);
    }

}
