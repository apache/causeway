package org.apache.causeway.extensions.pdfjs.metamodel;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

class PlatformTransactionManagerNoop implements PlatformTransactionManager {
    @Override
    public void rollback(final TransactionStatus status) throws TransactionException {
    }

    @Override
    public TransactionStatus getTransaction(final TransactionDefinition definition) throws TransactionException {
        return null;
    }

    @Override
    public void commit(final TransactionStatus status) throws TransactionException {
    }
}
