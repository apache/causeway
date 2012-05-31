package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.commands;

import javax.persistence.EntityManager;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandContext;

public abstract class AbstractJpaObjectCommand extends PersistenceCommandAbstract {
    
    private final EntityManager entityManager;

    AbstractJpaObjectCommand(final ObjectAdapter adapter,
            final EntityManager entityManager) {
        super(adapter);
        this.entityManager = entityManager;
        
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }
    
    public abstract void execute(final PersistenceCommandContext context);

}