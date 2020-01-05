package org.apache.isis.runtime.persistence.transaction.events;

import java.util.EventObject;

import org.apache.isis.runtime.persistence.transaction.IsisTransactionObject;

import lombok.Getter;

public abstract class TransactionEventAbstract extends EventObject {

    public enum Type {
        BEFORE_BEGIN,
        AFTER_BEGIN,
        BEFORE_COMMIT,
        AFTER_COMMIT,
        BEFORE_ROLLBACK,
        AFTER_ROLLBACK,
    }

    /**
     * Same as {@link #getSource()}.
     */
    @Getter
    private final IsisTransactionObject isisTransactionObject;

    @Getter
    private final Type type;

    public TransactionEventAbstract(
            final IsisTransactionObject source,
            final Type type) {
        super(source);
        this.isisTransactionObject = source;
        this.type = type;
    }


}
