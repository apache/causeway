package org.apache.isis.extensions.jpa.metamodel.facets.prop.manytoone;

import javax.persistence.FetchType;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.extensions.jpa.metamodel.facets.JpaFetchTypeFacetAbstract;


public class JpaFetchTypeFacetDerivedFromJpaManyToOneAnnotation extends
        JpaFetchTypeFacetAbstract {

    public JpaFetchTypeFacetDerivedFromJpaManyToOneAnnotation(
            final FetchType fetchType, final FacetHolder holder) {
        super(holder, fetchType);
    }

}
