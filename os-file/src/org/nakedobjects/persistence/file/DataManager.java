/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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

package org.nakedobjects.persistence.file;

import java.util.Enumeration;

import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.SerialOid;


public abstract class DataManager {
	/**
	 * Save the data for an object and adds the reference to a list of instances
	 */
	public final void insert(Data data) throws ObjectStoreException {
		if (data.getOid() == null) {
			throw new IllegalArgumentException("Oid must be non-null");
		}

		String type = data.getClassName();
		SerialOid oid = data.getOid();
		addData(oid, type, data);
		addInstance(oid, type);
	}

	public final SerialOid createOid() throws PersistorException {
		return new SerialOid(nextId());
	}

	/**
	 * Loads in data for a collection for the specified identifier.
	 */
	public final CollectionData loadCollectionData(SerialOid oid) {
		return (CollectionData) loadData(oid);
	}
	
	/**
	 * Loads in data for an object for the specified identifier.
	 */
	public final ObjectData loadObjectData(SerialOid oid) {
		return (ObjectData) loadData(oid);
	}
	
	/**
	 * Return data for all instances that match the pattern. 
	 */
	// TODO we need to be able to find collection instances as well
	protected abstract ObjectDataVector getInstances(ObjectData pattern);
	
	public final void remove(SerialOid oid) throws ObjectNotFoundException, ObjectStoreException {
		Data data = loadData(oid);
		String type = data.getClassName();
		removeInstance(oid, type);
		deleteData(oid, type);
	}

	/**
	 * Save the data for latter retrieval.
	 */
	public final void save(Data data) throws ObjectStoreException {
		updateData(data.getOid(), data.getClassName(), data);
	}

	/**
	 * Read in the next unique number for the object identifier.
	 */
	protected abstract long nextId() throws PersistorException;

	protected abstract Data loadData(SerialOid oid);

	/**
	 * Add the reference for an instance to the list of all instances.
	 */
	protected abstract void addInstance(SerialOid oid, String type) throws ObjectStoreException;

	/**
	 * Remove the reference for an instance from the list of all instances.
	 */
	protected abstract void removeInstance(SerialOid oid, String type) throws ObjectStoreException;

	/**
	 * Write out the data for a new instance.
	 */
	protected abstract void addData(SerialOid oid, String type, Data data)
	throws ObjectStoreException;

	/**
	 * Delete the data for an existing instance.
	 */
	protected abstract void deleteData(SerialOid oid, String type)
	throws ObjectStoreException;

	/**
	 * Write out the data for an existing instance.
	 */
	protected abstract void updateData(SerialOid oid, String type, Data data)
	throws ObjectStoreException;

	/** 
	 * Return the number of instances that match the specified data
	 */
	// TODO we need to be able to find collection instances as well
	protected abstract int numberOfInstances(ObjectData pattern) ;

	/**
	 * A helper that determines if two sets of data match.  A match occurs when the 
	 * types are the same and any field in the pattern also occurs in the data set 
	 * under test.
	 */
	// TODO we need to be able to find collection instances as well
	protected boolean matchesPattern(ObjectData patternData, ObjectData testData) {
		if(patternData == null || testData == null) {
			throw new NullPointerException("Can't match on nulls " + patternData + " " + testData);
		}
		if(! patternData.getClassName().equals(testData.getClassName())) {
			return false;
		}
		
		Enumeration fields = patternData.fields();

		while(fields.hasMoreElements()) {
			String field = (String) fields.nextElement();
			Object patternFieldValue = patternData.get(field);
			
			Object testFieldValue = testData.get(field);
			
			if(testFieldValue instanceof ReferenceVector) {
				ReferenceVector patternElements = (ReferenceVector) patternFieldValue;
				for (int i = 0; i < patternElements.size(); i++) {
					SerialOid requiredElement = patternElements.elementAt(i);  // must have this element
					boolean requiredFound = false;
					ReferenceVector testElements = ((ReferenceVector) testFieldValue);
					for (int j = 0; j < testElements.size(); j++) {
						if(requiredElement.equals(testElements.elementAt(j))) {
							requiredFound = true;
							break;
						}
					}
					if(!requiredFound) {
						return false;
					}
				}
			} else {
				if(!patternFieldValue.equals(testFieldValue)) {
					return false;
				}
			}
			
		}

		return true;
	}
	
	public void shutdown() {
	}

    public void getNakedClass(String name) {}
	
}
