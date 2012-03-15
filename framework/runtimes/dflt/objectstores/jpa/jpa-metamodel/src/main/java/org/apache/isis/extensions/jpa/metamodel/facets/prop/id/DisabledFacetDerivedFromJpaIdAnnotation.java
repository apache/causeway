package org.apache.isis.extensions.jpa.metamodel.facets.prop.id;

import javax.persistence.Transient;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.When;
import org.apache.isis.core.progmodel.facets.members.disable.DisabledFacetImpl;


/**
 * Derived from being {@link Transient}.
 */
public class DisabledFacetDerivedFromJpaIdAnnotation extends DisabledFacetImpl {

    public DisabledFacetDerivedFromJpaIdAnnotation(final FacetHolder holder) {
        super(When.ALWAYS, holder);
    }

}
