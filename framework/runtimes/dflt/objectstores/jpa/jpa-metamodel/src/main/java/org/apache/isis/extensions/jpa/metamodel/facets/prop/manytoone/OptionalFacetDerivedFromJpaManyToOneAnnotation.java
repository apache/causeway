package org.apache.isis.extensions.jpa.metamodel.facets.prop.manytoone;

import javax.persistence.ManyToOne;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.mandatory.MandatoryFacetAbstract;


/**
 * Derived by presence of {@link ManyToOne#optional()} set to <tt>true</tt>
 * .
 * <p>
 * This implementation indicates that the {@link FacetHolder} is <i>not</i>
 * mandatory, as per {@link #isInvertedSemantics()}.
 */
public class OptionalFacetDerivedFromJpaManyToOneAnnotation extends
        MandatoryFacetAbstract {

    public OptionalFacetDerivedFromJpaManyToOneAnnotation(
            final FacetHolder holder) {
        super(holder);
    }

    /**
     * Always returns <tt>false</tt>, indicating that the facet holder is in
     * fact optional.
     */
    public boolean isRequiredButNull(final ObjectAdapter adapter) {
        return false;
    }

    public boolean isInvertedSemantics() {
        return true;
    }

}

// Copyright (c) Naked Objects Group Ltd.
