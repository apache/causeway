package org.nakedobjects.persistence.cache;

import org.nakedobjects.object.DirtyObjectSet;
import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.defaults.AbstracObjectPersistenceManager;
import org.nakedobjects.object.defaults.NullDirtyObjectSet;
import org.nakedobjects.object.io.Memento;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.utility.Assert;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import test.org.nakedobjects.object.repository.object.persistence.ObjectStoreException;


public class CacheObjectManager extends AbstracObjectPersistenceManager {
    private Journal journal;
    private final Hashtable nakedClasses = new Hashtable();
    private ObjectStore objectStore;
    
    private OidGenerator oidGenerator;
    
    private DirtyObjectSet objectsToRefreshViewsFor = new NullDirtyObjectSet();

    private SnapshotFactory snapshotFactory;

    public void abortTransaction() {
    // Need to restore all changed objects
    }

    public void addObjectChangedListener(DirtyObjectSet listener) {
        Assert.assertNotNull("must set a listener", listener);
        this.objectsToRefreshViewsFor = listener;
    }

    public void destroyObject(NakedObject object) {
        journal.writeJournal("delete", new Memento(object));
        objectStore.instances(object.getSpecification()).remove(object);
    }

    public void endTransaction() {
    //   Object transaction = null;
    //   journal.writeJournal("commit", transaction);
    }

    public String getDebugData() {
        return null;
    }

    public String getDebugTitle() {
        return null;
    }

    protected NakedObject[] getInstances(InstancesCriteria criteria) throws ObjectStoreException, UnsupportedFindException {
        // TODO deal with subclasses
        Vector instances = new Vector();
        Enumeration objects = objectStore.instances(criteria.getSpecification()).instances();
        while (objects.hasMoreElements()) {
            NakedObject instance = (NakedObject) objects.nextElement();
            if (criteria.matches(instance)) {
                instances.addElement(instance);
            }
        }

        return toArray(instances);
    }

    protected NakedObject[] getInstances(NakedObjectSpecification specification, boolean includeSubclasses) {
        // TODO deal with subclasses
        Vector instances = new Vector();
        Enumeration objects = objectStore.instances(specification).instances();
        while (objects.hasMoreElements()) {
            NakedObject instance = (NakedObject) objects.nextElement();
            instances.addElement(instance);
        }

        return toArray(instances);
    }

    public NakedClass getNakedClass(NakedObjectSpecification specification) {
        if (nakedClasses.contains(specification)) {
            return (NakedClass) nakedClasses.get(specification);
        }

        NakedClass spec;
        spec = new NakedClass(specification.getFullName());
        nakedClasses.put(specification, spec);
        return spec;
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) {
        NakedObject object = (NakedObject) objectStore.instances(hint).read(oid);
        if (object == null) {
            throw new ObjectNotFoundException(oid);
        }
        return object;
    }

    public boolean hasInstances(NakedObjectSpecification specification) {
        return numberOfInstances(specification) > 0;
    }

    public void init() {
        objectStore = new ObjectStore(snapshotFactory);
        objectStore.init();
        
        journal.applyJournals();
        journal.openJounal();
    }

    public void makePersistent(NakedObject object) {
        NakedObjects.getObjectLoader().madePersistent(object, oidGenerator.next(object));

        journal.writeJournal("create", new Memento(object));
        objectStore.instances(object.getSpecification()).create(object);

    }

    public String name() {
        return "Cache Object Manager/Store";
    }

    public int numberOfInstances(NakedObjectSpecification specification) {
        return objectStore.instances(specification).numberInstances();
    }

    public void objectChanged(NakedObject object) {
        if (!object.getResolveState().isIgnoreChanges()) {
            journal.writeJournal("save", new Memento(object));
            objectsToRefreshViewsFor.addDirty(object);
        }
    }

    public void reload(NakedObject object) {
    // do nothing
    }

    public void reset() {}

    public void resolveImmediately(NakedObject object) {
    // do nothing
    }

    public void resolveField(NakedObject object, NakedObjectField field) {
    // do nothing
    }

    public void saveChanges() {
    // done by objectChanged
    }

    public void setJournal(Journal journal) {
        this.journal = journal;
    }

    public void setOidGenerator(OidGenerator oidGenerator) {
        this.oidGenerator = oidGenerator;
    }
    
    public void setSnapshotFactory(SnapshotFactory snapshotFactory) {
        this.snapshotFactory = snapshotFactory;
    }
    
    public void shutdown() {
        objectStore.shutdown();
        
        journal.closeJournal();
    }

    public void startTransaction() {}

    private NakedObject[] toArray(Vector instances) {
        NakedObject[] instanceArray = new NakedObject[instances.size()];
        instances.copyInto(instanceArray);
        return instanceArray;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */