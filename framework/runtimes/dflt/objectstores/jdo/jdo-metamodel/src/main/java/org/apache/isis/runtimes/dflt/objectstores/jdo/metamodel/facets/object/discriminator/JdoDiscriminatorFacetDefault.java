package org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.discriminator;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleValueFacetAbstract;

public class JdoDiscriminatorFacetDefault extends SingleValueFacetAbstract<String> implements JdoDiscriminatorFacet {

    public JdoDiscriminatorFacetDefault(String value, FacetHolder holder) {
        super(JdoDiscriminatorFacet.class, value, holder);
    }

}
