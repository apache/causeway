package org.apache.isis.applib.services.iactn;

import org.apache.isis.applib.services.wrapper.WrapperFactory;

/**
 * Enumerates the different reasons for a sequence of occurrences within a
 * single (top-level) {@link Interaction}.
 *
 * @since 1.x {@index}
 */
public enum SequenceType {

    /**
     * Numbers the executions (an action invocation or property edit) within
     * a given {@link Interaction}.
     *
     * <p>
     * Each {@link Interaction} is initiated by an execution of action
     * invocation or a property edit.  Thereafter there could be multiple
     * other executions as the result of nested calls using the
     * {@link WrapperFactory}.
     * </p>
     *
     * <p>
     * Another possible reason is support for bulk action invocations.
     * </p>
     *
     * @see Interaction
     * @see WrapperFactory
     */
    EXECUTION,


    /**
     * Numbers the transactions within a given {@link Interaction}.
     *
     * <p>
     * Each {@link Interaction} is executed within the context of a transaction, but
     * the (occasionally) the transaction may be committed and a new one
     * started as the result of the domain object using the
     * {@link org.apache.isis.applib.services.xactn.TransactionService}.
     * </p>
     *
     * @see Interaction
     * @see org.apache.isis.applib.services.xactn.TransactionService
     */
    TRANSACTION,
    ;

    public String id() {
        return SequenceType.class.getName() + "#" + name();
    }
}
