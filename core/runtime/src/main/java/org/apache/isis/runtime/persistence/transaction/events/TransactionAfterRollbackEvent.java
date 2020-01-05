package org.apache.isis.runtime.persistence.transaction.events;

import java.util.EventObject;

import org.apache.isis.runtime.persistence.transaction.IsisTransactionObject;

import lombok.Getter;

public class TransactionAfterRollbackEvent extends TransactionEventAbstract {

    public TransactionAfterRollbackEvent(final IsisTransactionObject source) {
        super(source, Type.AFTER_ROLLBACK);
    }
}