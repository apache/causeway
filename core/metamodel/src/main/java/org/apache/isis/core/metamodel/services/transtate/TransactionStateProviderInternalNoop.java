package org.apache.isis.core.metamodel.services.transtate;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.core.metamodel.transactions.TransactionState;

@DomainService(nature = NatureOfService.DOMAIN)
public class TransactionStateProviderInternalNoop implements TransactionStateProviderInternal {

    @Override
    public TransactionState getTransactionState() {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }
}
