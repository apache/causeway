package org.nakedobjects.persistence.sql;

import org.nakedobjects.object.NakedObject;


public interface CollectionMapper {

	public void loadInternalCollection(DatabaseConnector connector, NakedObject parent);

	public void saveInternalCollection(DatabaseConnector connector, NakedObject parent);
	
	void createTables(DatabaseConnector connection) throws SqlObjectStoreException ;

	boolean needsTables(DatabaseConnector connection) throws SqlObjectStoreException;
}