package org.apache.isis.core.security.authorization.manager;

import java.util.List;

import org.apache.isis.core.security.authorization.Authorizor;

/**
 * Provides an SPI to select from multiple {@link Authorizor}s if more than
 * one are present on the classpath.
 *
 * @since 2.0 {@index}
 */
@FunctionalInterface
public interface AuthorizorChooser {

    /**
     *
     * @param authorizors
     */
    Authorizor chooseFrom(final List<Authorizor> authorizors);
}
