package org.apache.isis.extensions.jpa.metamodel.facets.prop.column;

import javax.persistence.Column;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.maxlen.MaxLengthFacetAbstract;


/**
 * Derived from {@link Column#length()}.
 */
public class MaxLengthFacetDerivedFromJpaColumnAnnotation extends
        MaxLengthFacetAbstract {

    public MaxLengthFacetDerivedFromJpaColumnAnnotation(final int value,
            final FacetHolder holder) {
        super(value, holder);
    }

}

// Copyright (c) Naked Objects Group Ltd.
