package org.apache.isis.runtimes.dflt.objectstores.datanucleus.persistence.commands;

import javax.jdo.PersistenceManager;
import javax.persistence.EntityManager;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandContext;

public abstract class AbstractDataNucleusObjectCommand extends PersistenceCommandAbstract {
    
    private final PersistenceManager persistenceManager;

    AbstractDataNucleusObjectCommand(final ObjectAdapter adapter,
            final PersistenceManager persistenceManager) {
        super(adapter);
        this.persistenceManager = persistenceManager;
        
    }

    protected PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }
    
    public abstract void execute(final PersistenceCommandContext context);

}