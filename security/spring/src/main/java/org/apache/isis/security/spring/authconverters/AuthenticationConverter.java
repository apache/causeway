package org.apache.isis.security.spring.authconverters;

import org.springframework.security.core.Authentication;

import org.apache.isis.applib.services.user.UserMemento;

/**
 * Defines an SPI to attempt to convert a Spring {@link Authentication} into
 * an Apache Isis {@link UserMemento}.
 *
 * <p>
 *     The {@link Authentication} will have already been verified as having been
 *     {@link Authentication#isAuthenticated() authenticated}.
 * </p>
 *
 * <p>
 *     Implementations should be defined as Spring {@link Component}s
 *     and added to the {@link Configuration application context}
 *     either by being {@link Import imported} explicitly
 *     or  implicitly through {@link ComponentScan}.
 * </p>
 *
 * <p>
 *     All known converters are checked one by one, but checking stops once one
 *     converter has successively converted the {@link Authentication} into a
 *     {@link UserMemento} (in other words, chain-of-responsibility pattern).
 *     Use the {@link org.springframework.core.Ordered} to influence the order
 *     in which converter implementations are checked.
 * </p>
 *
 * @since 2.0 {@index}
 */
public interface AuthenticationConverter {

    /**
     * Attempt to convert a Spring {@link Authentication} (which will have been
     * {@link Authentication#isAuthenticated() authenticated}) into a
     * {@link UserMemento}.
     *
     * <p>
     *     There are many different implementations of {@link Authentication},
     *     so the implementation should be targetted at a specific
     *     implementation.
     * </p>
     *
     * <p>
     *     The framework provides some default implementations for the most
     *     common use cases.
     * </p>
     *
     * @param authentication to attempt to convert
     * @return non-null if could be converted
     */
    UserMemento convert(final Authentication authentication);
}
