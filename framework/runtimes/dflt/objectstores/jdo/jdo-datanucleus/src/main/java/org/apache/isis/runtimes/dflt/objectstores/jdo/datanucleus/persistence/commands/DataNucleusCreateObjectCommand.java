package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.commands;

import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandContext;

public class DataNucleusCreateObjectCommand extends AbstractDataNucleusObjectCommand implements CreateObjectCommand {

    private static final Logger LOG = Logger
            .getLogger(DataNucleusCreateObjectCommand.class);

    public DataNucleusCreateObjectCommand(ObjectAdapter adapter, PersistenceManager persistenceManager) {
        super(adapter, persistenceManager);
    }


    @Override
    public void execute(final PersistenceCommandContext context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create object - executing command for: " + onAdapter());
        }
        final ObjectAdapter adapter = onAdapter();
        final Object domainObject = adapter.getObject();

        getPersistenceManager().makePersistent(domainObject);
    }

    @Override
    public String toString() {
        return "CreateObjectCommand [adapter=" + onAdapter() + "]";
    }

}
