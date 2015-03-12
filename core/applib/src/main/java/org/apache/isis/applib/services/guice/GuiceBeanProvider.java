package org.apache.isis.applib.services.guice;

import java.lang.annotation.Annotation;
import org.apache.isis.applib.annotation.DomainService;

/**
 * A domain service acting as a bridge between Isis services and Guice.
 */
@DomainService
public interface GuiceBeanProvider {

    /**
     * Looks up a Guice bean by class type
     *
     * @param beanType The class type of the Guice bean
     * @param <T> The type of the Guice bean
     * @return The resolved bean
     */
    <T> T lookup(Class<T> beanType);

    /**
     * Looks up a Guice bean by class type
     *
     * @param beanType The class type of the Guice bean
     * @param qualifier  An annotation identifying the bean instance
     * @param <T> The type of the Guice bean
     * @return The resolved bean
     */
    <T> T lookup(Class<T> beanType, final Annotation qualifier);
}
