package org.apache.isis.core.metamodel.facets;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;

public interface ObjectSpecIdFacetFactory extends FacetFactory {

    // //////////////////////////////////////
    // process objectSpecId
    // //////////////////////////////////////

    public static class ProcessObjectSpecIdContext extends AbstractProcessWithClsContext<FacetHolder> {
        public ProcessObjectSpecIdContext(final Class<?> cls, final FacetHolder facetHolder) {
            super(cls, facetHolder);
        }
    }

    void process(final ProcessObjectSpecIdContext processClassContext);

}
