package org.apache.isis.core.metamodel.facets.actions.action.invocation;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;

public class ActionRequestCachedResultFacetFactory extends FacetFactoryAbstract {
    
    public ActionRequestCachedResultFacetFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final FacetHolder holder = processMethodContext.getFacetHolder();
        ActionRequestCachedResultFacet actionRequestCachedResultFacet = ActionRequestCachedResultFacetForActionAnnotation.create(processMethodContext, holder);
        if(actionRequestCachedResultFacet != null) {
            FacetUtil.addFacet(actionRequestCachedResultFacet);
        }
    }

}
