package org.nakedobjects.persistence.sql2;

import java.sql.SQLException;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.ResolveException;


public interface CollectionMapper {

	public void loadInternalCollection(NakedObject parent) throws ResolveException, SQLException;

	public void saveInternalCollection(NakedObject parent) throws ObjectStoreException;
	
	void createTables() throws ObjectStoreException ;

	boolean needsTables() throws ObjectStoreException;
}