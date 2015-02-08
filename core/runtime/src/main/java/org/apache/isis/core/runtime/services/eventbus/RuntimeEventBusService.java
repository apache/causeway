package org.apache.isis.core.runtime.services.eventbus;

import javax.enterprise.context.RequestScoped;

import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.runtime.services.RequestScopedService;

/**
 * Holds common runtime logic for EventBusService implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class RuntimeEventBusService extends EventBusService {
    
    //region > register
    /**
     * {@inheritDoc}
     *
     * This service overrides the method to perform additional validation that (a) request-scoped services register
     * their proxies, not themselves, and (b) that singleton services are never registered after the event bus has
     * been created.
     *
     * <p>
     *     Note that we <i>do</i> allow for request-scoped services to register (their proxies) multiple times, ie at
     *     the beginning of each transaction.  Because the subscribers are stored in a set, these additional
     *     registrations are in effect ignored.
     * </p>
     */
    @Override
    public void register(final Object domainService) {
        if(domainService instanceof RequestScopedService) {
            // ok; allow to be registered multiple times (each xactn) since stored in a set.
        } else {
            if (Annotations.getAnnotation(domainService.getClass(), RequestScoped.class) != null) {
                throw new IllegalArgumentException("Request-scoped services must register their proxy, not themselves");
            }
            // a singleton
            if(this.eventBus != null) {
                // ... coming too late to the party.
                throw new IllegalStateException("Event bus has already been created; too late to register any further (singleton) subscribers");
            }
        }
        super.register(domainService);
    }
    
    //endregion

}
