package org.apache.isis.applib.services.eventbus;


/**
 * Common interface for all Event Bus implementations.
 *
 * <p>
 *     Defines an SPI to the {@link org.apache.isis.applib.services.eventbus.EventBusService}, to allow alternative
 *     implementations of in-memory event bus to be used. Currently, there are implementations based on Guava and on
 *     the Axon framework.
 * </p>
 */
public interface EventBusImplementation {

    /**
     * For {@link org.apache.isis.applib.services.eventbus.EventBusService} to call on
     * {@link org.apache.isis.applib.services.eventbus.EventBusService#register(Object)}.
     */
    void register(Object domainService);

    /**
     * For {@link org.apache.isis.applib.services.eventbus.EventBusService} to call on
     * {@link org.apache.isis.applib.services.eventbus.EventBusService#unregister(Object)}.
     */
    void unregister(Object domainService);

    /**
     * For {@link org.apache.isis.applib.services.eventbus.EventBusService} to call on
     * {@link org.apache.isis.applib.services.eventbus.EventBusService#post(Object)}.
     */
    void post(Object event);

}