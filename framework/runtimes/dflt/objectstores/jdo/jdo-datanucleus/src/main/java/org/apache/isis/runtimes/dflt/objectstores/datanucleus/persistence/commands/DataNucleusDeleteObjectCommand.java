package org.apache.isis.runtimes.dflt.objectstores.datanucleus.persistence.commands;

import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandContext;

public class DataNucleusDeleteObjectCommand extends AbstractDataNucleusObjectCommand implements DestroyObjectCommand {

    private static final Logger LOG = Logger.getLogger(DataNucleusDeleteObjectCommand.class);

    public DataNucleusDeleteObjectCommand(ObjectAdapter adapter, PersistenceManager persistenceManager) {
        super(adapter, persistenceManager);
    }

    @Override
    public void execute(final PersistenceCommandContext context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy object - executing command for " + onAdapter());
        }
        getPersistenceManager().deletePersistent(onAdapter().getObject());
    }

    @Override
    public String toString() {
        return "DestroyObjectCommand [adapter=" + onAdapter() + "]";
    }

}
