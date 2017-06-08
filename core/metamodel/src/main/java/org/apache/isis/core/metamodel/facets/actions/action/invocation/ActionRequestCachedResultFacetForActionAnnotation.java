package org.apache.isis.core.metamodel.facets.actions.action.invocation;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actions.action.hidden.HiddenFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class ActionRequestCachedResultFacetForActionAnnotation extends FacetAbstract implements ActionRequestCachedResultFacet {

    public ActionRequestCachedResultFacetForActionAnnotation(Class<? extends Facet> facetType, FacetHolder holder, Derivation derivation) {
        super(facetType, holder, derivation);
    }
    
    public static ActionRequestCachedResultFacet create(
            ProcessMethodContext processMethodContext,
            FacetHolder holder) {

        Action actionAnnotation = processMethodContext.getMethod().getAnnotation(Action.class);
        
        return actionAnnotation != null && actionAnnotation.semantics().equals(SemanticsOf.SAFE_AND_REQUEST_CACHED) ? new ActionRequestCachedResultFacetForActionAnnotation(ActionRequestCachedResultFacet.class, holder, Derivation.DERIVED): null;
    }
        
}
