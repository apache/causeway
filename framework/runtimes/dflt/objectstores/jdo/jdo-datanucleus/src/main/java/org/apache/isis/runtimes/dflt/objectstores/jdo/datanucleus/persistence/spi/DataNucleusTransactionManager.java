package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.spi;

import javax.annotation.Nullable;

import org.apache.isis.runtimes.dflt.objectstores.jdo.applib.AuditService;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.TransactionalResource;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.EnlistedObjectDirtying;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransaction;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.MessageBroker;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.UpdateNotifier;

public class DataNucleusTransactionManager extends IsisTransactionManager {
    
    @Nullable
    private final AuditService auditService;

    public DataNucleusTransactionManager(
            final EnlistedObjectDirtying objectPersistor, 
            final TransactionalResource objectStore, 
            @Nullable final AuditService auditService) {
        super(objectPersistor, objectStore);
        this.auditService = auditService;
    }
    
    @Override
    protected IsisTransaction createTransaction(final MessageBroker messageBroker, final UpdateNotifier updateNotifier) {
        return new DataNucleusTransaction(this, messageBroker, updateNotifier, getTransactionalResource(), auditService);
    }

}
