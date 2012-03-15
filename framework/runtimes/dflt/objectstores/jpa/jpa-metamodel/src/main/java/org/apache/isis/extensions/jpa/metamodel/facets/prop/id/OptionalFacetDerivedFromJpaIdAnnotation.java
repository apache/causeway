package org.apache.isis.extensions.jpa.metamodel.facets.prop.id;

import javax.persistence.Id;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.mandatory.MandatoryFacetDefault;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.extensions.jpa.metamodel.facets.prop.version.OptionalFacetDerivedFromJpaVersionAnnotation;


/**
 * Derived by presence of {@link Id}; optional to allow Hibernate to detect
 * transient objects.
 * <p>
 * By default mandatory properties are initialized using the
 * {@link PropertyDefaultFacet} facet. We don't want this, so this facet marks
 * the property as optional, meaning that the {@link Id} property is left
 * untouched by Naked Objects.
 * 
 * @see OptionalFacetDerivedFromJpaVersionAnnotation
 */
public class OptionalFacetDerivedFromJpaIdAnnotation extends
        MandatoryFacetDefault {

    public OptionalFacetDerivedFromJpaIdAnnotation(final FacetHolder holder) {
        super(holder);
    }

    @Override
    public boolean isInvertedSemantics() {
        return true;
    }


}

// Copyright (c) Naked Objects Group Ltd.
