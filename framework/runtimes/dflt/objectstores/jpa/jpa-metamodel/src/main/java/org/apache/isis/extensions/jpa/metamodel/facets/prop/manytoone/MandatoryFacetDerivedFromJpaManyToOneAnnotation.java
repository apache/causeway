package org.apache.isis.extensions.jpa.metamodel.facets.prop.manytoone;

import javax.persistence.ManyToOne;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.mandatory.MandatoryFacetDefault;


/**
 * Derived by presence of {@link ManyToOne#optional()} set to
 * <tt>false</tt>.
 * <p>
 * This implementation indicates that the {@link FacetHolder} is mandatory.
 */
public class MandatoryFacetDerivedFromJpaManyToOneAnnotation extends
        MandatoryFacetDefault {

    public MandatoryFacetDerivedFromJpaManyToOneAnnotation(
            final FacetHolder holder) {
        super(holder);
    }
}

// Copyright (c) Naked Objects Group Ltd.
