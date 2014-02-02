package org.apache.isis.applib.services.background;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Programmatic;

/**
 * Submit actions to be invoked in the background.
 * 
 * <p>
 * Example usage:
 * <pre>
 * public void submitInvoices() {
 *     for(Customer customer: customerRepository.findCustomersToInvoice()) {
 *         backgroundService.execute(customer).submitInvoice();
 *     }
 * }
 * 
 * &#64;javax.inject.Inject
 * private BackgroundService backgroundService;
 * </pre>
 */
public interface BackgroundService {

    /**
     * Returns a proxy around the object which is then used to obtain the
     * signature of the action to be invoked in the background.
     */
    @Programmatic
    <T> T execute(final T object);
    
    @Programmatic
    ActionInvocationMemento asActionInvocationMemento(Method m, Object domainObject, Object[] args);

    @Programmatic
    ActionInvocationMemento newActionInvocationMemento(String memento);

}
