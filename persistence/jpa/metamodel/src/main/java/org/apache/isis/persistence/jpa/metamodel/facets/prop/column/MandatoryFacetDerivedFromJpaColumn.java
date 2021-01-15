package org.apache.isis.persistence.jpa.metamodel.facets.prop.column;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacetAbstract;

/**
 * Derived by presence of an <tt>@Column</tt> method.
 */
public class MandatoryFacetDerivedFromJpaColumn extends MandatoryFacetAbstract {


    public MandatoryFacetDerivedFromJpaColumn(final FacetHolder holder, final boolean required) {
        super(holder, Semantics.of(required));
    }


}