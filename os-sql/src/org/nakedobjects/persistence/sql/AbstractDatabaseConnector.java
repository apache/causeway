package org.nakedobjects.persistence.sql;

import org.apache.log4j.Logger;


public abstract class AbstractDatabaseConnector implements DatabaseConnector {
    private static final Logger LOG = Logger.getLogger(AbstractDatabaseConnector.class);
    private boolean isUsed;

    public final void setUsed(boolean isUsed) {
        this.isUsed = isUsed;
    }

    public final boolean isUsed() {
        return isUsed;
    }

    private int transactionLevel = 0;
    
    public final  void startTransaction() {
        transactionLevel ++;
    }

    public final void endTransaction() {
        transactionLevel --;
    }

    public final boolean isTransactionComplete() {
        return transactionLevel == 0;
    }
    
    private DatabaseConnectorPool pool;
    
    public final void setConnectionPool(DatabaseConnectorPool pool) {
        this.pool = pool;
    }
    
    public final DatabaseConnectorPool getConnectionPool() {
        return pool;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */