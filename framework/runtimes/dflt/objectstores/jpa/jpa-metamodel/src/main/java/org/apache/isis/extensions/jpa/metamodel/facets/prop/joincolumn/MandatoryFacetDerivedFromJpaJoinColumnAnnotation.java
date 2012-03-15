package org.apache.isis.extensions.jpa.metamodel.facets.prop.joincolumn;

import javax.persistence.Basic;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.mandatory.MandatoryFacetDefault;


/**
 * Derived by presence of {@link Basic#optional()} set to <tt>false</tt>.
 * <p>
 * This implementation indicates that the {@link FacetHolder} is mandatory.
 */
public class MandatoryFacetDerivedFromJpaJoinColumnAnnotation extends
        MandatoryFacetDefault {

    public MandatoryFacetDerivedFromJpaJoinColumnAnnotation(
            final FacetHolder holder) {
        super(holder);
    }

}

// Copyright (c) Naked Objects Group Ltd.
