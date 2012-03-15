package org.apache.isis.extensions.jpa.metamodel.facets.prop.transience;

import javax.persistence.Transient;

import org.apache.isis.applib.events.UsabilityEvent;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.notpersisted.NotPersistedFacetAbstract;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;


/**
 * Derived from being {@link Transient}.
 */
public class DerivedFacetDerivedFromJpaTransientAnnotation extends
		NotPersistedFacetAbstract {

    public DerivedFacetDerivedFromJpaTransientAnnotation(
            final FacetHolder holder) {
        super(holder);
    }

    /**
     * Always returns <i>Derived</i>.
     */
    public String disables(final UsabilityContext<? extends UsabilityEvent> ic) {
        return "JPA transient";
    }

}
