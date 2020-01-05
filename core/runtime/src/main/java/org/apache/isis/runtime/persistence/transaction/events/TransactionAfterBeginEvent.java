package org.apache.isis.runtime.persistence.transaction.events;

import org.apache.isis.runtime.persistence.transaction.IsisTransactionObject;

import lombok.Getter;

public class TransactionAfterBeginEvent extends TransactionEventAbstract {

    public TransactionAfterBeginEvent(final IsisTransactionObject source) {
        super(source, Type.AFTER_BEGIN);
    }

}
