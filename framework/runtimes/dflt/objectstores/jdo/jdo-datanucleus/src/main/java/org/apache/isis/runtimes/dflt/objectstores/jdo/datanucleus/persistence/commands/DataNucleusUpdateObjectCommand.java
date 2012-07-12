package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.commands;

import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.SaveObjectCommand;

public class DataNucleusUpdateObjectCommand extends AbstractDataNucleusObjectCommand implements SaveObjectCommand {
    private static final Logger LOG = Logger
            .getLogger(DataNucleusDeleteObjectCommand.class);

    public DataNucleusUpdateObjectCommand(ObjectAdapter adapter, PersistenceManager persistenceManager) {
        super(adapter, persistenceManager);
    }

    @Override
    public void execute(final PersistenceCommandContext context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("save object - executing command for: " + onAdapter());
        }
        
        // TODO: this might be a no-op; JDO doesn't seem to have an equivalent of JPA's merge() ...
        // getEntityManager().merge(onAdapter().getObject());
    }

    @Override
    public String toString() {
        return "SaveObjectCommand [adapter=" + onAdapter() + "]";
    }
}
