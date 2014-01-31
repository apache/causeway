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

    void execute(final ActionInvocationMemento actionInvocationMemento, final UUID transactionId, final int sequence);
}
