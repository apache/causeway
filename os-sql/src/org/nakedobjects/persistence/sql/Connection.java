package org.nakedobjects.persistence.sql;

public final class Connection {
    private DatabaseConnectorPool pool;
    
    
    public Connection(DatabaseConnectorPool connectionPool) {
        pool = connectionPool;
    }
/*
    public Results callStoredProcedure(String name, Parameter[] parameters) {
        DatabaseConnector connection = pool.acquire();
        Results results = connection.callStoredProcedure(name, parameters);
        pool.release(connection);
        return results;
    }
*/
    public int count(String sql) throws SqlObjectStoreException {
        DatabaseConnector connection = pool.acquire();
        int count = connection.count(sql);
        pool.release(connection);
        return count;
    }

    public void delete(String sql) throws SqlObjectStoreException {
        DatabaseConnector connection = pool.acquire();
        connection.delete(sql);
        pool.release(connection);
    }

    /*
    public MultipleResults executeStoredProcedure(String name, Parameter[] parameters) {
        DatabaseConnector connection = pool.acquire();
        MultipleResults results = connection.executeStoredProcedure(name, parameters);
        pool.release(connection);
        return results;
    }
*/
    public boolean hasTable(String tableName) throws SqlObjectStoreException {
        DatabaseConnector connection = pool.acquire();
        boolean hasTable = connection.hasTable(tableName);
        pool.release(connection);
        return hasTable;
    }

    public void insert(String sql) throws SqlObjectStoreException {
        DatabaseConnector connection = pool.acquire();
        connection.insert(sql);
        pool.release(connection);    
    }

    public void insert(String sql, Object oid) throws SqlObjectStoreException {
        DatabaseConnector connection = pool.acquire();
        connection.insert(sql, oid);
        pool.release(connection);    
    }

    public Results select(String sql) {
        DatabaseConnector connection = pool.acquire();
        Results results = connection.select(sql);
        pool.release(connection);
        return results;
    }

    public void update(String sql) throws SqlObjectStoreException {
        DatabaseConnector connection = pool.acquire();
        connection.update(sql);
        pool.release(connection);    
    }
    
    public DatabaseConnector acquire() {
    	return pool.acquire();
    }
    
    public void release(DatabaseConnector connector) {
    	pool.release(connector);
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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