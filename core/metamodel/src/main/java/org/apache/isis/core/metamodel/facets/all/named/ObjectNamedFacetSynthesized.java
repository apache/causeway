package org.apache.isis.core.metamodel.facets.all.named;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.i8n.noun.NounForms;

/**
 * To be installed by facet-post-processing only,
 * which in any case must provide both noun-forms (non-empty).
 *
 * @since 2.0
 */
public class ObjectNamedFacetSynthesized
extends ObjectNamedFacetAbstract {

    public ObjectNamedFacetSynthesized(
            final NounForms nounForms,
            final FacetHolder holder) {
        super(nounForms, holder, Precedence.SYNTHESIZED);
    }

}
