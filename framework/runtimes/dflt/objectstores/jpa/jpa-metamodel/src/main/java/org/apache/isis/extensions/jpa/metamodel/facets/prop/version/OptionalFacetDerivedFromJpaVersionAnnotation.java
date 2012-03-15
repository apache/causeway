package org.apache.isis.extensions.jpa.metamodel.facets.prop.version;

import javax.persistence.Version;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.mandatory.MandatoryFacetDefault;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.extensions.jpa.metamodel.facets.prop.id.OptionalFacetDerivedFromJpaIdAnnotation;


/**
 * Derived by presence of {@link Version}; optional to allow Hibernate to detect
 * transient objects.
 * <p>
 * By default mandatory properties are initialized using the
 * {@link PropertyDefaultFacet} facet. We don't want this, so this facet marks
 * the property as optional, meaning that the {@link Version} property is left
 * untouched by Naked Objects.
 * 
 * @see OptionalFacetDerivedFromJpaIdAnnotation
 */
public class OptionalFacetDerivedFromJpaVersionAnnotation extends
        MandatoryFacetDefault {

    public OptionalFacetDerivedFromJpaVersionAnnotation(final FacetHolder holder) {
        super(holder);
    }

    @Override
    public boolean isInvertedSemantics() {
        return true;
    }


}

// Copyright (c) Naked Objects Group Ltd.
