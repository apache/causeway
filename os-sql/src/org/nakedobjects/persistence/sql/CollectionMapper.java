package org.nakedobjects.persistence.sql;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.ResolveException;


public interface CollectionMapper {

	public void loadInternalCollection(NakedObject parent) throws ResolveException, ObjectStoreException;

	public void saveInternalCollection(NakedObject parent) throws ObjectStoreException;
	
	void createTables() throws ObjectStoreException ;

	boolean needsTables() throws ObjectStoreException;
}