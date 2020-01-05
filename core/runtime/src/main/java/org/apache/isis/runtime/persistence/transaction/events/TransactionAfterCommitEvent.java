package org.apache.isis.runtime.persistence.transaction.events;

import java.util.EventObject;

import org.apache.isis.runtime.persistence.transaction.IsisTransactionObject;

import lombok.Getter;

public class TransactionAfterCommitEvent extends TransactionEventAbstract {

    public TransactionAfterCommitEvent(final IsisTransactionObject source) {
        super(source, Type.AFTER_COMMIT);
    }
}
