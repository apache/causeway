package org.nakedobjects.persistence.cache;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.persistence.ObjectStoreException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.PojoAdapterFactory;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;


class Instances {
    private final static Logger LOG = Logger.getLogger(Instances.class);
    private NakedObjectSpecification specification;
    private Hashtable index = new Hashtable();
    private Vector orderedInstances = new Vector();

    public Instances(NakedObjectSpecification cls) {
        if(cls == null) {
            throw new NullPointerException();
        }
        this.specification = cls;
    }
    
    public void create(NakedObject object) {
        orderedInstances.addElement(object);
        index.put(object.getOid(), object);
    }

    public Enumeration instances() {
        return orderedInstances.elements();
    }

    public int loadData(SnapshotImpl reader) throws ObjectStoreException {
        int noInstances = reader.readInt();
        int size = 0;
        for (int i = 0; i < noInstances; i++) {
            Memento memento = (Memento) reader.readObject();
            LOG.debug("read 2: " + i + " " + memento);

            NakedObject object = loadedObjects().getLoadedObject(memento.getOid());
            memento.updateObject(object);
            LOG.debug("recreated " + object + " " + object.titleString());
            size++;
        }
        return size;
    }

    private PojoAdapterFactory loadedObjects() {
        return NakedObjects.getObjectLoader();
    }

    public void loadIdentities(SnapshotImpl reader) throws ObjectStoreException {
        int noInstances = reader.readInt();
        for (int i = 0; i < noInstances; i++) {
            Oid oid = (Oid) reader.readOid();
            LOG.debug("read 1: " + i + " " + specification.getFullName() + "/" + oid);

            NakedObject obj = objectLoader.recreateAdapterForPersistent(oid, specification);
            
//            NakedObject obj = (NakedObject) specification.acquireInstance();
//            obj.setOid(oid);
            preload(oid, obj);
//            loadedObjects().loaded(obj);
        }
    }

    public int numberInstances() {
        return orderedInstances.size();
    }

    private void preload(Object oid, NakedObject object) {
        orderedInstances.addElement(object);
        index.put(oid, object);
    }

    public NakedObject read(Object oid) {
        NakedObject object = (NakedObject) index.get(oid);
        if (object == null) { throw new NakedObjectRuntimeException("No object for " + oid); }
        return object;
    }

    public void remove(NakedObject object) {
        orderedInstances.remove(object);
        index.remove(object.getOid());
    }

    public long saveData(SnapShotWriter writer) throws ObjectStoreException {
        writer.writeInt(numberInstances());

        Enumeration e = instances();
        int i = 0;
        while (e.hasMoreElements()) {
            NakedObject object = (NakedObject) e.nextElement();
            writer.writeNakedObject(object);
        }
        return i;
    }

    public void saveIdentities(SnapShotWriter writer) throws ObjectStoreException {
        writer.writeInt(numberInstances());

        Enumeration e = instances();
        int i = 0;
        while (e.hasMoreElements()) {
            NakedObject object = (NakedObject) e.nextElement();
            Object oid = object.getOid();
            writer.writeOid(oid);
            LOG.debug("write 1: " + i++ + " " + specification.getFullName() + "/" + oid);
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
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