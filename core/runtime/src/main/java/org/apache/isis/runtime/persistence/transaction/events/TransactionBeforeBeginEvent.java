package org.apache.isis.runtime.persistence.transaction.events;

import org.apache.isis.runtime.persistence.transaction.IsisTransactionObject;

public class TransactionBeforeBeginEvent extends TransactionEventAbstract {

    public TransactionBeforeBeginEvent(final IsisTransactionObject source) {
        super(source, Type.BEFORE_BEGIN);
    }

}
