package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.spi;

import java.util.List;

import org.apache.isis.runtimes.dflt.objectstores.jdo.applib.AuditService;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.TransactionalResource;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.EnlistedObjectDirtying;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransaction;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.MessageBroker;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.UpdateNotifier;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class DataNucleusTransactionManager extends IsisTransactionManager {

    private static final Predicate<Object> AUDIT_SERVICE = new Predicate<Object>() {

        @Override
        public boolean apply(Object input) {
            return input instanceof AuditService;
        }
    };

    public DataNucleusTransactionManager(EnlistedObjectDirtying objectPersistor, TransactionalResource objectStore) {
        super(objectPersistor, objectStore);
    }
    
    @Override
    protected IsisTransaction createTransaction(MessageBroker messageBroker, UpdateNotifier updateNotifier) {
        DataNucleusTransaction transaction = new DataNucleusTransaction(this, messageBroker, updateNotifier, getTransactionalResource());
        List<Object> services = IsisContext.getServices();
        Optional<Object> optionalService = Iterables.tryFind(services, AUDIT_SERVICE);
        if(optionalService.isPresent()) {
            AuditService service = (AuditService) optionalService.get();
            transaction.usingAuditService(service);
        }
        return transaction;
    }

}
