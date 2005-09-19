package org.nakedobjects.persistence.cache;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.persistence.ObjectNotFoundException;
import org.nakedobjects.object.persistence.ObjectStoreException;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

public class ObjectStore {
    private final static Logger LOG = Logger.getLogger(ObjectStore.class);

    private Hashtable objectSets;
    private final SnapshotFactory factory;
    
    
    public ObjectStore(SnapshotFactory factory) {
        this.factory = factory;
    }
    
    public void init() {
        loadSnapshot();
     }
    
    Instances instances(NakedObjectSpecification spec) {
        String name = spec.getFullName();
        if (objectSets.containsKey(name)) {
            return (Instances) objectSets.get(name);
        } else {
            Instances index = new Instances(spec);
            objectSets.put(name, index);
            return index;
        }
    }

    private Instances instances(String className) throws ObjectNotFoundException {
        if (objectSets.containsKey(className)) {
            return (Instances) objectSets.get(className);
        } else {
            throw new ObjectNotFoundException();
        }
    }

    private int loadData(SnapshotReader reader, NakedObjectLoader loader) {
        int size = 0;
        int noClasses = reader.readInt();
        for (int k = 0; k < noClasses; k++) {
            String className = (String) reader.readClassName();
            size += instances(className).loadData(reader, loader);
        }
        return size;
    }

    private int loadInstances(SnapshotReader reader, NakedObjectLoader loader) {
        int noClasses = reader.readInt();
        for (int k = 0; k < noClasses; k++) {
            String className = (String) reader.readClassName();
            Instances instances = instances(className);
            instances.loadIdentities(reader, loader);
        }
        return noClasses;
    }

    private void loadSnapshot() throws ObjectStoreException {
        objectSets = new Hashtable();

        NakedObjectLoader loader = NakedObjects.getObjectLoader();
        
        SnapshotReader snapshot = factory.createReader(); 
        
        if(snapshot.open()) {
	        loadInstances(snapshot, loader);
	        int size = loadData(snapshot, loader);
	        LOG.info(size + " objects loaded from " + snapshot);
    } else {
            LOG.info("No snapshot to load: " + snapshot);
        }

    }


    private void saveData(SnapshotWriter writer) throws ObjectStoreException {
        long size = 0;
        writer.writeInt(objectSets.size());
        Enumeration e1 = objectSets.keys();
        while (e1.hasMoreElements()) {
            String className = (String) e1.nextElement();
            writer.writeClassName(className);
            Instances instances = (Instances) objectSets.get(className);
            size += instances.saveData(writer);
        }
        LOG.info(size + " objects saved");
    }

    private void saveIdentities(SnapshotWriter writer) throws ObjectStoreException {
        writer.writeInt(objectSets.size());
        Enumeration e1 = objectSets.keys();
        while (e1.hasMoreElements()) {
            String className = (String) e1.nextElement();
            writer.writeClassName(className);
            instances(className).saveIdentities(writer);
        }
    }


    private void saveSnapshot() throws ObjectStoreException {
        SnapshotWriter writer = factory.createWriter();
        writer.open();
        saveIdentities(writer);
        saveData(writer);
        writer.close();
    }
    
    public void shutdown() {
        saveSnapshot();    
    }

    public int numberInstances(NakedObjectSpecification specification) {
        return  instances(specification).numberInstances();
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