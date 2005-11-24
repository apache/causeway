package org.nakedobjects.persistence.sql;

import java.util.Vector;

import org.apache.log4j.Logger;

public class DatabaseConnectorPool {
    private static final Logger LOG = Logger.getLogger(DatabaseConnectorPool.class);
    private static final int AVERAGE_POOL_SIZE = 5;

    private final DatabaseConnectorFactory factory;
    private final Vector connectorPool;


    public DatabaseConnectorPool(DatabaseConnectorFactory factory) throws SqlObjectStoreException {
        this(factory, AVERAGE_POOL_SIZE);
    }

    public DatabaseConnectorPool(DatabaseConnectorFactory factory, int size) throws SqlObjectStoreException {
        this.factory = factory;
        connectorPool = new Vector();
        for (int i = 0; i < size; i++) {
            newConnector();
        }
        LOG.info("Created an intial pool of " + size + " database connections");
    }
    
    private DatabaseConnector newConnector() throws SqlObjectStoreException {
        DatabaseConnector connector = factory.createConnector();
        connector.setConnectionPool(this);
        connector.open();
        connectorPool.addElement(connector);
        return connector;
    }

    public DatabaseConnector acquire() throws SqlObjectStoreException {
        DatabaseConnector connector = findFreeConnector();
        if(connector == null) {
            connector = newConnector();
            connector.setUsed(true);
            LOG.info("Added an additional database connection; now contains " + connectorPool.size()+ " connections");
        }
        LOG.debug("acquired connection " + connector);
        return connector;
    }
    
    private DatabaseConnector findFreeConnector() {
        for (int i = 0, no = connectorPool.size(); i < no; i++) {
            DatabaseConnector connector = (DatabaseConnector) connectorPool.elementAt(i);
            if(!connector.isUsed()) {
                connector.setUsed(true);
                return connector;
            }
        }
        return null;
    }

    public void release(DatabaseConnector connector) {
        connector.setUsed(false);
        LOG.debug("released connection " + connector);
    }
    
    public void shutdown() {
        for (int i = 0, no = connectorPool.size(); i < no; i++) {
            DatabaseConnector connector = (DatabaseConnector) connectorPool.elementAt(i);
            try {
                connector.close();
            } catch (SqlObjectStoreException e) {
                LOG.error("Failed to release connectuion", e);
            }
        }
        connectorPool.removeAllElements();
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/