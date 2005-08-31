package org.nakedobjects.persistence.file;

import org.nakedobjects.object.persistence.ObjectNotFoundException;
import org.nakedobjects.object.persistence.ObjectManagerException;
import org.nakedobjects.object.persistence.defaults.SerialOid;


public interface DataManager {
    SerialOid createOid() throws PersistorException;

    void getNakedClass(String name);

    /**
     * Save the data for an object and adds the reference to a list of instances
     */
    void insert(Data data) throws ObjectManagerException;

    /**
     * Loads in data for a collection for the specified identifier.
     */
    CollectionData loadCollectionData(SerialOid oid);

    /**
     * Loads in data for an object for the specified identifier.
     */
    ObjectData loadObjectData(SerialOid oid);

    void remove(SerialOid oid) throws ObjectNotFoundException, ObjectManagerException;

    /**
     * Save the data for latter retrieval.
     */
    void save(Data data) throws ObjectManagerException;

    void shutdown();
    
    /**
     * Return data for all instances that match the pattern.
     */
    public ObjectDataVector getInstances(ObjectData pattern);
    
    public Data loadData(SerialOid oid);
    
    /**
    * Return the number of instances that match the specified data
    */
   public int numberOfInstances(ObjectData pattern);

    String getDebugData();
}

/*
 Naked Objects - a framework that exposes behaviourally complete
 business objects directly to the user.
 Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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