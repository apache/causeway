package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.commands;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandContext;

public class JpaDeleteObjectCommand extends AbstractJpaObjectCommand implements DestroyObjectCommand {

    private static final Logger LOG = Logger.getLogger(JpaDeleteObjectCommand.class);

    public JpaDeleteObjectCommand(ObjectAdapter adapter, EntityManager entityManager) {
        super(adapter, entityManager);
    }

    @Override
    public void execute(final PersistenceCommandContext context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy object - executing command for " + onAdapter());
        }
        getEntityManager().remove(onAdapter().getObject());
    }

    @Override
    public String toString() {
        return "DestroyObjectCommand [adapter=" + onAdapter() + "]";
    }

}
