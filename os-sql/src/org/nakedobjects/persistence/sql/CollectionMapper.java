package org.nakedobjects.persistence.sql;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ResolveException;


public interface CollectionMapper {

	public void loadInternalCollection(NakedObject parent) throws ResolveException, SqlObjectStoreException;

	public void saveInternalCollection(NakedObject parent) throws SqlObjectStoreException;
	
	void createTables() throws SqlObjectStoreException ;

	boolean needsTables() throws SqlObjectStoreException;
}