package org.apache.isis.extensions.jpa.metamodel.facets.collection.elements;

import javax.persistence.FetchType;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.extensions.jpa.metamodel.facets.JpaFetchTypeFacetAbstract;


public class JpaFetchTypeFacetDerivedFromJpaElementCollectionsAnnotation
        extends JpaFetchTypeFacetAbstract {

    public JpaFetchTypeFacetDerivedFromJpaElementCollectionsAnnotation(
            final FacetHolder holder, final FetchType fetchType) {
        super(holder, fetchType);
    }

}
