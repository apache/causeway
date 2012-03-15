package org.apache.isis.extensions.jpa.metamodel.facets.object.discriminator;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.progmodel.facets.object.objecttype.ObjectTypeFacetAbstract;


public class ObjectTypeFacetInferredFromJpaDiscriminatorValueAnnotation extends
        ObjectTypeFacetAbstract {

    public ObjectTypeFacetInferredFromJpaDiscriminatorValueAnnotation(final String value,
            final FacetHolder holder) {
        super(value, holder);
    }

}

// Copyright (c) Naked Objects Group Ltd.
