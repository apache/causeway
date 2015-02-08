package org.apache.isis.core.runtime.services.eventbus;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.objectstore.jdo.datanucleus.JDOStateManagerForIsis;
import org.apache.isis.objectstore.jdo.datanucleus.JDOStateManagerForIsis.Hint;


/**
 * This domain service that enables both the framework and application code to
 * publish events through an Axon
 * {@link org.axonframework.eventhandling.SimpleEventBus} instance.
 * 
 * <p>
 * In addition, this implementation is &quot;JDO-aware&quot; meaning that it
 * allows events to be {@link #post(Object) posted} from the setters of
 * entities, automatically ignoring any calls to those setters that occur as a
 * side-effect of the JDO load/detach lifecycle.
 * 
 * <p>
 * This implementation has no UI.
 */
@DomainService(nature=NatureOfService.DOMAIN)
public class AxonSimpleEventBusServiceJdo extends AxonSimpleEventBusService {

    /**
     * skip if called in any way by way of the {@link JDOStateManagerForIsis}.
     * 
     * <p>
     * The {@link JDOStateManagerForIsis} sets a
     * {@link JDOStateManagerForIsis#hint threadlocal} if it has been called.
     */
    @Override
    public boolean skip(final Object event) {
        return JDOStateManagerForIsis.hint.get() != Hint.NONE;
    }

}