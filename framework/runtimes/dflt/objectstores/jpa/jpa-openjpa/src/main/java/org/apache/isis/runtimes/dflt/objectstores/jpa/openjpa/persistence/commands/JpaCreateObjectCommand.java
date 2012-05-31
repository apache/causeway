package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.commands;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandContext;

public class JpaCreateObjectCommand extends AbstractJpaObjectCommand implements CreateObjectCommand {

    private static final Logger LOG = Logger
            .getLogger(JpaCreateObjectCommand.class);

    public JpaCreateObjectCommand(ObjectAdapter adapter, EntityManager entityManager) {
        super(adapter, entityManager);
    }


    @Override
    public void execute(final PersistenceCommandContext context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create object - executing command for: " + onAdapter());
        }
        final ObjectAdapter adapter = onAdapter();
        final Object domainObject = adapter.getObject();

        getEntityManager().persist(domainObject);
    }

    @Override
    public String toString() {
        return "CreateObjectCommand [adapter=" + onAdapter() + "]";
    }

}
