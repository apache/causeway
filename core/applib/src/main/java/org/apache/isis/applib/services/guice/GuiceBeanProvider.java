package org.apache.isis.applib.services.guice;

import org.apache.isis.applib.annotation.DomainService;

/**
 * A domain service acting as a bridge between Isis services and Guice.
 */
@DomainService
public interface GuiceBeanProvider {

    /**
     * Looks up a Guice bean by class type
     *
     * @param cls The class type of the Guice bean
     * @param <T> The type of the Guice bean
     * @return The found
     */
    <T> T lookup(Class<T> cls);
}
