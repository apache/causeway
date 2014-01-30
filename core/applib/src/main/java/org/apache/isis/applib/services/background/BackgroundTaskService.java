package org.apache.isis.applib.services.background;

import java.util.UUID;


/**
 * Execute a {@link ActionInvocationMemento memento-ized} action as a
 * decoupled task.
 * 
 * <p>
 * Separate from {@link BackgroundService} primarily so that the default
 * implementation, <tt>BackgroundServiceDefault</tt> (in core-runtime) can
 * delegate to different implementations of this service.
 */
public interface BackgroundTaskService {

    /**
     * @param actionInvocationMemento
     * @param transactionId - if any.
     */
    void execute(ActionInvocationMemento actionInvocationMemento, UUID transactionId);
}
