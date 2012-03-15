package org.apache.isis.extensions.jpa.metamodel.facets.prop.onetoone;

import javax.persistence.OneToOne;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.mandatory.MandatoryFacetDefault;


/**
 * Derived by presence of {@link OneToOne#optional()} set to <tt>false</tt>
 * .
 * <p>
 * This implementation indicates that the {@link FacetHolder} is mandatory.
 */
public class MandatoryFacetDerivedFromJpaOneToOneAnnotation extends
        MandatoryFacetDefault {

    public MandatoryFacetDerivedFromJpaOneToOneAnnotation(
            final FacetHolder holder) {
        super(holder);
    }
}

// Copyright (c) Naked Objects Group Ltd.
