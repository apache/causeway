package org.nakedobjects.persistence.file;

import java.util.Hashtable;
import java.util.Vector;

import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.defaults.SerialOid;


public class InMemoryDataManager extends DataManager {
    private Hashtable objects = new Hashtable();
    private Hashtable indexes = new Hashtable();
    private int nextId = 100;

    protected long nextId() throws PersistorException {
        nextId++;

        return nextId;
    }

    protected void writeInstanceFile(String name, Vector instances) {
        indexes.put(name, instances);
    }

    protected void deleteData(SerialOid oid, String type) {
        objects.remove(oid);
    }

    protected Data loadData(SerialOid oid) {
        if (!objects.containsKey(oid)) { return null; }

        return (Data) objects.get(oid);
    }

    protected void addInstance(SerialOid oid, String type) {
        Vector instances = (Vector) indexes.get(type);

        if (instances == null) {
            instances = new Vector();
            indexes.put(type, instances);
        }

        instances.addElement(oid);
    }

    protected void addData(SerialOid oid, String type, Data data) throws ObjectStoreException {
        if (data.getOid() == null) { throw new IllegalArgumentException("Oid must be non-null"); }

        objects.put(oid, data);
    }

    protected void updateData(SerialOid oid, String type, Data data) throws ObjectStoreException {
        objects.put(oid, data);
    }

    protected int numberOfInstances(ObjectData pattern) {
        Vector instances = (Vector) indexes.get(pattern.getClassName());

        if (instances == null) { return 0; }

        int instanceCount = 0;

        for (int i = 0; i < instances.size(); i++) {
            SerialOid oid = (SerialOid) instances.elementAt(i);
            ObjectData instanceData = (ObjectData) objects.get(oid);

            if (matchesPattern(pattern, instanceData)) {
                instanceCount++;
            }
        }

        return instanceCount;
    }

    protected ObjectDataVector getInstances(ObjectData pattern) {
        Vector instances = (Vector) indexes.get(pattern.getClassName());

        if (instances == null) { return new ObjectDataVector(); }

        ObjectDataVector matches = new ObjectDataVector();

        for (int i = 0; i < instances.size(); i++) {
            SerialOid oid = (SerialOid) instances.elementAt(i);
            ObjectData instanceData = (ObjectData) objects.get(oid);

            if (matchesPattern(pattern, instanceData)) {
                matches.addElement(instanceData);
            }
        }

        return matches;
    }

    protected void removeInstance(SerialOid oid, String type) throws ObjectStoreException {
        Vector instances = (Vector) indexes.get(type);
        instances.removeElement(oid);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2003 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */