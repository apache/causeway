package org.apache.isis.extensions.base.dom;

import org.apache.isis.applib.services.factory.FactoryService;

/**
 * For <tt>enum</tt>s that act as powertypes, in other words acting as a factory
 * for subtypes of some inheritance hierarchy.
 */
public interface PowerType<T> {

    T create(FactoryService factoryService);

}
