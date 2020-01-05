package org.apache.isis.runtime.persistence.transaction.events;

import java.util.EventObject;

import org.apache.isis.runtime.persistence.transaction.IsisTransactionObject;

import lombok.Getter;

public class TransactionBeforeRollbackEvent extends TransactionEventAbstract {

    public TransactionBeforeRollbackEvent(final IsisTransactionObject source) {
        super(source, Type.BEFORE_ROLLBACK);
    }
}