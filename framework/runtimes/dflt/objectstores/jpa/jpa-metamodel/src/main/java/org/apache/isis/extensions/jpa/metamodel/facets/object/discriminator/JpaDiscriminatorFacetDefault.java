package org.apache.isis.extensions.jpa.metamodel.facets.object.discriminator;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleValueFacetAbstract;

public class JpaDiscriminatorFacetDefault extends SingleValueFacetAbstract<String> implements JpaDiscriminatorFacet {

    public JpaDiscriminatorFacetDefault(String value, FacetHolder holder) {
        super(JpaDiscriminatorFacet.class, value, holder);
    }

}
