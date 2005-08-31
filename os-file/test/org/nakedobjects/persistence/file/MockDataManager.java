package org.nakedobjects.persistence.file;

import org.nakedobjects.object.persistence.ObjectNotFoundException;
import org.nakedobjects.object.persistence.ObjectManagerException;
import org.nakedobjects.object.persistence.defaults.SerialOid;

import java.util.Vector;

import junit.framework.Assert;

public class MockDataManager implements DataManager {
    private Vector actions = new Vector();
    
    public void assertAction(int i, String action) {
        if (i >= actions.size()) {
            Assert.fail("No such action " + action);
        }
  //      Assert.assertEquals(action, actions.elementAt(i));
    }

  

    public MockDataManager() {
        super();
    }

    public SerialOid createOid() throws PersistorException {
        return null;
    }

    public void getNakedClass(String name) {}

    public void insert(Data data) throws ObjectManagerException {}

    public CollectionData loadCollectionData(SerialOid oid) {
        return null;
    }

    public ObjectData loadObjectData(SerialOid oid) {
        return null;
    }

    public void remove(SerialOid oid) throws ObjectNotFoundException, ObjectManagerException {}

    public void save(Data data) throws ObjectManagerException {
        actions.addElement(data);
    }

    public void shutdown() {}

    public ObjectDataVector getInstances(ObjectData pattern) {
        return null;
    }

    public Data loadData(SerialOid oid) {
        return null;
    }

    public int numberOfInstances(ObjectData pattern) {
        actions.addElement(pattern.getClassName());
        
        return 5;
    }



    public String getDebugData() {
        return null;
    }

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