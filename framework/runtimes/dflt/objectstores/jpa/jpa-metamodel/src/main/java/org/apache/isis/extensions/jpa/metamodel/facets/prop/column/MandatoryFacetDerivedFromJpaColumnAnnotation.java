package org.apache.isis.extensions.jpa.metamodel.facets.prop.column;

import javax.persistence.Column;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.mandatory.MandatoryFacetDefault;


/**
 * Derived by presence of {@link Column#nullable()} set to <tt>false</tt>.
 * <p>
 * This implementation indicates that the {@link FacetHolder} is mandatory.
 */
public class MandatoryFacetDerivedFromJpaColumnAnnotation extends
        MandatoryFacetDefault {

    public MandatoryFacetDerivedFromJpaColumnAnnotation(final FacetHolder holder) {
        super(holder);
    }

}

// Copyright (c) Naked Objects Group Ltd.
