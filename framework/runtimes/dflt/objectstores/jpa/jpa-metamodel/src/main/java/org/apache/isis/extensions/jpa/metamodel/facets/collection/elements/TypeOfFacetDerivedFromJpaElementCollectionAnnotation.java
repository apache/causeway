package org.apache.isis.extensions.jpa.metamodel.facets.collection.elements;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacetAbstract;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;


/**
 * Derived from {@link CollectionOfElements#targetElement()}.
 */
public class TypeOfFacetDerivedFromJpaElementCollectionAnnotation
        extends TypeOfFacetAbstract {

    public TypeOfFacetDerivedFromJpaElementCollectionAnnotation(
            final FacetHolder holder,
            final Class<?> elementType,
            final SpecificationLoader specificationLoader) {
        super(elementType, holder, specificationLoader);
    }


}

// Copyright (c) Naked Objects Group Ltd.
