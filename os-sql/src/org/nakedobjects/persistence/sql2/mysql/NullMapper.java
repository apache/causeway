package org.nakedobjects.persistence.sql2.mysql;

import java.util.Vector;

import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.persistence.sql2.ObjectMapper;


public class NullMapper implements ObjectMapper {

	public void createObject(NakedObject object) throws ObjectStoreException {
	}

	public void destroyObject(NakedObject object) throws ObjectStoreException {
	}

	public void save(NakedObject object) throws ObjectStoreException {
	}

	public NakedObject getObject(Object oid, NakedClass hint) throws ObjectNotFoundException, ObjectStoreException {
		return null;
	}

	public void resolve(NakedObject object) throws ObjectStoreException {
	}

	public Vector getInstances(NakedClass cls) throws ObjectStoreException {
		return  new Vector();
	}

	public Vector getInstances(NakedClass cls, String pattern) throws ObjectStoreException, UnsupportedFindException {
		return  new Vector();
	}

	public Vector getInstances(NakedObject pattern) throws ObjectStoreException, UnsupportedFindException {
		return new Vector();
	}

	public boolean hasInstances(NakedClass cls) throws ObjectStoreException {
		return false;
	}

	public int numberOfInstances(NakedClass cls) throws ObjectStoreException {
		return 0;
	}

	public void startup(LoadedObjects loadedObjects) throws ObjectStoreException {
	}

	public void shutdown() throws ObjectStoreException {
	}

}
