package org.apache.isis.applib.services.iactn;

import org.apache.isis.applib.services.wrapper.WrapperFactory;

/**
 * Enumerates the different reasons why multiple occurrences of a certain type might occur within a single
 * (top-level) interaction.
 *
 * @since 1.x {@index}
 */
public enum Sequence {

    /**
     * Each interaction is either an action invocation or a property edit.  There could be multiple of these,
     * typically as the result of a nested calls using the {@link WrapperFactory}.  Another reason is
     * support for bulk action invocations within a single transaction.
     */
    INTERACTION,

    /**
     * For objects: multiple such could be dirtied and thus published as separate events.  For actions
     * invocations/property edits : multiple sub-invocations could occur if sub-invocations are made through the
     * {@link WrapperFactory}.
     */
    PUBLISHED_EVENT,

    /**
     * There may be multiple transactions within a given interaction.
     */
    TRANSACTION,
    ;

    public String id() {
        return Sequence.class.getName() + "#" + name();
    }
}
