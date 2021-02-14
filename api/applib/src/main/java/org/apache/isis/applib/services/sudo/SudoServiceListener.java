package org.apache.isis.applib.services.sudo;

import org.apache.isis.applib.services.iactn.ExecutionContext;

import lombok.NonNull;

/**
 * Allows the {@link SudoService} to notify other services/components that
 * the effective user has been changed.
 *
 * <p>
 * The subscribing domain service need only implement this interface,
 * there is no need to explicitly register as a subscriber.
 * </p>
 *
 * @since 2.0
 */
public interface SudoServiceListener {

    /**
     * @param before
     * @param after
     */
    void beforeCall(@NonNull ExecutionContext before, @NonNull ExecutionContext after);

    void afterCall(@NonNull ExecutionContext before, @NonNull ExecutionContext after);
}
