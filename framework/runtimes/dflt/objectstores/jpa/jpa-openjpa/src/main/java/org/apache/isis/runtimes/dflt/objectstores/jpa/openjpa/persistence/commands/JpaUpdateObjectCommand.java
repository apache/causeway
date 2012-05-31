package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.commands;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.SaveObjectCommand;

public class JpaUpdateObjectCommand extends AbstractJpaObjectCommand implements SaveObjectCommand {
    private static final Logger LOG = Logger
            .getLogger(JpaDeleteObjectCommand.class);

    public JpaUpdateObjectCommand(ObjectAdapter adapter, EntityManager entityManager) {
        super(adapter, entityManager);
    }

    @Override
    public void execute(final PersistenceCommandContext context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("save object - executing command for: " + onAdapter());
        }
        getEntityManager().merge(onAdapter().getObject());
    }

    @Override
    public String toString() {
        return "SaveObjectCommand [adapter=" + onAdapter() + "]";
    }
}
