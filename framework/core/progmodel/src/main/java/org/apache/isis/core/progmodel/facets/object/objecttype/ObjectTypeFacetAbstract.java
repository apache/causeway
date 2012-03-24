package org.apache.isis.core.progmodel.facets.object.objecttype;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleStringValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.objecttype.ObjectTypeFacet;


public abstract class ObjectTypeFacetAbstract extends
        SingleStringValueFacetAbstract implements ObjectTypeFacet {

    public static Class<? extends Facet> type() {
        return ObjectTypeFacet.class;
    }

    public ObjectTypeFacetAbstract(final String value,
            final FacetHolder holder) {
        super(ObjectTypeFacetAbstract.type(), holder, value);
    }
}
