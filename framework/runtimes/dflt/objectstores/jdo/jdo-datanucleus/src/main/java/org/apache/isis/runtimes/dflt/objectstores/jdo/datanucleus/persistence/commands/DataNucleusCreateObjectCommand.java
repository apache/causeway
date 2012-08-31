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
        if(!adapter.isTransient()) {
            // this could happen if DN's persistence-by-reachability has already caused the domainobject
            // to be persisted.  It's Isis adapter will have been updated as a result of the postStore
            // lifecycle callback, so in essence there's nothing to be done.
            return;
        }
        final Object domainObject = adapter.getObject();

        getPersistenceManager().makePersistent(domainObject);
    }

    @Override
    public String toString() {
        return "CreateObjectCommand [adapter=" + onAdapter() + "]";
    }

}
