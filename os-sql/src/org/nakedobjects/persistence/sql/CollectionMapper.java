package org.nakedobjects.persistence.sql;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ResolveException;


public interface CollectionMapper {

	public void loadInternalCollection(DatabaseConnector connector, NakedObject parent) throws ResolveException, SqlObjectStoreException;

	public void saveInternalCollection(DatabaseConnector connector, NakedObject parent) throws SqlObjectStoreException;
	
	void createTables(DatabaseConnector connection) throws SqlObjectStoreException ;

	boolean needsTables(DatabaseConnector connection) throws SqlObjectStoreException;
}