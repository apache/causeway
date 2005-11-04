package org.nakedobjects.persistence.cache;

import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ObjectPerstsistenceException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.io.Memento;
import org.nakedobjects.object.persistence.objectstore.NakedObjectStore;
import org.nakedobjects.object.transaction.CreateObjectCommand;
import org.nakedobjects.object.transaction.DestroyObjectCommand;
import org.nakedobjects.object.transaction.PersistenceCommand;
import org.nakedobjects.object.transaction.SaveObjectCommand;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;


// TODO empty naked values won't work properly (i think)
public class CacheObjectStore implements NakedObjectStore {
    private final static Logger LOG = Logger.getLogger(CacheObjectStore.class);
    private Hashtable objectSets;

    public void abortTransaction() {}

    public CreateObjectCommand createCreateObjectCommand(final NakedObject object) {
        return new CreateObjectCommand() {

            public void execute() throws ObjectPerstsistenceException {
                journal.writeJournal("create", new Memento(object));
                instances(object.getSpecification()).create(object);
            }

            public NakedObject onObject() {
                return object;
            }

            public String toString() {
                return "CreateObjectCommand [object=" + object + "]";
            }
        };
    }

    public DestroyObjectCommand createDestroyObjectCommand(final NakedObject object) {
        return new DestroyObjectCommand() {

            public void execute() throws ObjectPerstsistenceException {
                journal.writeJournal("delete", new Memento(object));
                instances(object.getSpecification()).remove(object);
            }

            public NakedObject onObject() {
                return object;
            }

            public String toString() {
                return "CreateObjectCommand [object=" + object + "]";
            }
        };
    }

    public SaveObjectCommand createSaveObjectCommand(final NakedObject object) {
        return new SaveObjectCommand() {
            public void execute() throws ObjectPerstsistenceException {
                journal.writeJournal("save", new Memento(object));
            }

            public NakedObject onObject() {
                return object;
            }

            public String toString() {
                return "CreateObjectCommand [object=" + object + "]";
            }
        };
    }

    public void endTransaction() {}

    public String getDebugData() {
        return null;
    }

    public String getDebugTitle() {
        return null;
    }

    
    public NakedObject[] getInstances(InstancesCriteria criteria) throws ObjectPerstsistenceException, UnsupportedFindException {
        // TODO deal with subclasses
        Vector instances = new Vector();
        Enumeration objects = instances(criteria.getSpecification()).instances();
        while (objects.hasMoreElements()) {
            NakedObject instance = (NakedObject) objects.nextElement();
            if (criteria.matches(instance)) {
                instances.addElement(instance);
            }
        }

        return toArray(instances);
    }

    public NakedObject[] getInstances(NakedObjectSpecification specification, boolean includeSubclasses) {
        // TODO deal with subclasses
        Vector instances = new Vector();
        Enumeration objects = instances(specification).instances();
        while (objects.hasMoreElements()) {
            NakedObject instance = (NakedObject) objects.nextElement();
            instances.addElement(instance);
        }

        return toArray(instances);
    }

    public NakedClass getNakedClass(String name) throws ObjectNotFoundException, ObjectPerstsistenceException {
        throw new ObjectNotFoundException();
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException, ObjectPerstsistenceException {
        NakedObject object = (NakedObject) instances(hint).read(oid);
        if (object == null) {
            throw new ObjectNotFoundException(oid);
        }
        return object;
    }

    public boolean hasInstances(NakedObjectSpecification spec, boolean includeSubclasses) {
        return numberOfInstances(spec, false) > 0;
    }

    private JournalImpl journal;
    private SnapshotImpl snapshot;
    
    public void setJournal(JournalImpl journal) {
        this.journal = journal;
    }
    
    public void setSnapshot(SnapshotImpl snapshot) {
        this.snapshot = snapshot;
    }
    
    public void init() throws ObjectPerstsistenceException {
        loadSnapshot();
        journal.applyJournals();
        journal.openJounal();
    }

    private Instances instances(NakedObjectSpecification spec) {
        String className = spec.getFullName();

        if (objectSets.containsKey(className)) {
            return (Instances) objectSets.get(className);
        } else {
            Instances index = new Instances(spec);
            objectSets.put(className, index);
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

    private int loadData(SnapshotImpl reader) throws ObjectPerstsistenceException {
        int size = 0;
        int noClasses = reader.readInt();
        for (int k = 0; k < noClasses; k++) {
            String className = (String) reader.readClassName();
            size += instances(className).loadData(reader);
        }
        return size;
    }

    private int loadInstances(SnapshotImpl reader) throws ObjectPerstsistenceException {
        int noClasses = reader.readInt();
        for (int k = 0; k < noClasses; k++) {
            String className = (String) reader.readClassName();
            Instances instances = instances(className);
            instances.loadIdentities(reader);
        }
        return noClasses;
    }

    private void loadSnapshot() throws ObjectPerstsistenceException {
        objectSets = new Hashtable();

        if(snapshot.open()) {
	        loadInstances(snapshot);
	        int size = loadData(snapshot);
	        LOG.info(size + " objects loaded from " + snapshot);
    } else {
            LOG.info("No snapshot to load: " + snapshot);
        }

    }

    public String name() {
        return "Cache Object Store";
    }

    public int numberOfInstances(NakedObjectSpecification spec, boolean includedSubclasses) {
        return instances(spec).numberInstances();
    }

    public void resolveField(NakedObject object, NakedObjectField field) throws ObjectPerstsistenceException {}

    public void resolveImmediately(NakedObject object) {}

    public void runTransaction(PersistenceCommand[] commands) throws ObjectPerstsistenceException {
        LOG.info("start execution of transaction");
        for (int i = 0; i < commands.length; i++) {
            PersistenceCommand command = commands[i];
            command.execute();
        }
        LOG.info("end execution");
    }

    private void saveData(SnapShotWriter writer) throws ObjectPerstsistenceException {
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

    private void saveIdentities(SnapShotWriter writer) throws ObjectPerstsistenceException {
        writer.writeInt(objectSets.size());
        Enumeration e1 = objectSets.keys();
        while (e1.hasMoreElements()) {
            String className = (String) e1.nextElement();
            writer.writeClassName(className);
            instances(className).saveIdentities(writer);
        }
    }

    private void saveSnapshot() throws ObjectPerstsistenceException {
        SnapshotWriter writer = new SnapshotWriter();
        writer.open();
        saveIdentities(writer);
        saveData(writer);
        writer.close();
    }

    public void shutdown() throws ObjectPerstsistenceException {
        saveSnapshot();
        journal.closeJournal();
    }

    public void startTransaction() {}

    private NakedObject[] toArray(Vector instances) {
        NakedObject[] instanceArray = new NakedObject[instances.size()];
        instances.copyInto(instanceArray);
        return instanceArray;
    }

    public void reset() {}
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
