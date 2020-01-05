package org.apache.isis.runtime.persistence.transaction.events;

import java.util.EventObject;

import org.apache.isis.runtime.persistence.transaction.IsisTransactionObject;

import lombok.Getter;

public class TransactionBeforeCommitEvent extends TransactionEventAbstract {

    public TransactionBeforeCommitEvent(final IsisTransactionObject source) {
        super(source, Type.BEFORE_COMMIT);
    }
}