package org.nakedobjects.persistence.sql;

public interface DatabaseConnector {
    /* @deprecated 
    Results callStoredProcedure(String name, Parameter[] parameters);
*/
    void close() throws SqlObjectStoreException;

    int count(String sql) throws SqlObjectStoreException;

    void delete(String sql) throws SqlObjectStoreException;

//    MultipleResults executeStoredProcedure(String name, Parameter[] parameters);

    boolean hasTable(String tableName) throws SqlObjectStoreException;

    void insert(String sql) throws SqlObjectStoreException;

    void insert(String sql, Object oid) throws SqlObjectStoreException;

    void open() throws SqlObjectStoreException;

    Results select(String sql);

    /**
     * Updates the database using the specified sql statement, and returns the number of rows affected.
     */
    int update(String sql) throws SqlObjectStoreException;
    
    void setUsed(boolean isUsed);
    
    boolean isUsed();

    void commit() throws SqlObjectStoreException;

    void rollback()  throws SqlObjectStoreException;

    void startTransaction();

    void endTransaction();

    boolean isTransactionComplete();

    void setConnectionPool(DatabaseConnectorPool pool) ;
    
    DatabaseConnectorPool getConnectionPool();
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