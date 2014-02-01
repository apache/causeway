package org.apache.isis.applib.services.background;

import java.lang.reflect.Method;

import org.apache.isis.applib.services.reifiableaction.ReifiableAction;


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

    void schedule(
            final ActionInvocationMemento aim, 
            final ReifiableAction reifiableAction, 
            final String targetClassName, final String targetActionName, final String targetArgs);
}
