package org.nakedobjects.object.persistence.objectore;

import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ObjectPerstsistenceException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.transaction.CreateObjectCommand;
import org.nakedobjects.object.transaction.DestroyObjectCommand;
import org.nakedobjects.object.transaction.PersistenceCommand;
import org.nakedobjects.object.transaction.SaveObjectCommand;
import org.nakedobjects.utility.Logger;
import org.nakedobjects.utility.configuration.ComponentException;
import org.nakedobjects.utility.configuration.ConfigurationException;


public class ObjectStoreLogger extends Logger implements NakedObjectStore {
    private final NakedObjectStore decorated;

    public ObjectStoreLogger(final NakedObjectStore decorated, final String logFileName) {
        super(logFileName, false);
        this.decorated = decorated;
    }

    public ObjectStoreLogger(final NakedObjectStore decorated) {
        super(null, true);
        this.decorated = decorated;
    }

    public void abortTransaction() throws ObjectPerstsistenceException {
        log("Abort transaction started");
        decorated.abortTransaction();
        log("Abort transaction complete");
    }

    public CreateObjectCommand createCreateObjectCommand(NakedObject object) {
        log("Create object " + object);
        return decorated.createCreateObjectCommand(object);
    }

    public DestroyObjectCommand createDestroyObjectCommand(NakedObject object) {
        log("Destroy object " + object);
        return decorated.createDestroyObjectCommand(object);
    }

    public SaveObjectCommand createSaveObjectCommand(NakedObject object) {
        log("Save object " + object);
        return decorated.createSaveObjectCommand(object);
    }

    public void endTransaction() throws ObjectPerstsistenceException {
        log("End transaction");
        decorated.endTransaction();
    }

    public String getDebugData() {
        return decorated.getDebugData();
    }

    public String getDebugTitle() {
        return decorated.getDebugTitle();
    }

    protected Class getDecoratedClass() {
        return decorated.getClass();
    }

    public NakedObject[] getInstances(InstancesCriteria criteria) throws ObjectPerstsistenceException, UnsupportedFindException {
        log("Get instances matching " + criteria);
        return decorated.getInstances(criteria);
    }

    public NakedObject[] getInstances(NakedObjectSpecification specification, boolean includeSubclasses)
            throws ObjectPerstsistenceException {
        log("Get instances of " + specification.getShortName());
        return decorated.getInstances(specification, includeSubclasses);
    }

    public NakedClass getNakedClass(String name) throws ObjectNotFoundException, ObjectPerstsistenceException {
        NakedClass cls = decorated.getNakedClass(name);
        log("Get class " + name, cls);
        return cls;
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException, ObjectPerstsistenceException {
        NakedObject object = decorated.getObject(oid, hint);
        log("Get object for " + oid + " (of type " + hint.getShortName() + ")", object.getObject());
        return object;
    }

    public boolean hasInstances(NakedObjectSpecification specification, boolean includeSubclasses) throws ObjectPerstsistenceException {
        boolean hasInstances = decorated.hasInstances(specification, includeSubclasses);
        log("Has instances of " + specification.getShortName(), "" + hasInstances);
        return hasInstances;
    }

    public void init() throws ConfigurationException, ComponentException, ObjectPerstsistenceException {
        log("Initialising " + name());
        decorated.init();
    }

    public String name() {
        return decorated.name();
    }

    public int numberOfInstances(NakedObjectSpecification specification, boolean includedSubclasses) throws ObjectPerstsistenceException {
        int number = decorated.numberOfInstances(specification, includedSubclasses);
        log("Number of instances of " + specification.getShortName(), "" + number);
        return number;
    }

    public void reset() {
        log("Reset");
        decorated.reset();
    }

    public void resolveField(NakedObject object, NakedObjectField field) throws ObjectPerstsistenceException {
        log("Resolve eagerly object in field " + field + " of " + object);
        decorated.resolveField(object, field);
    }

    public void resolveImmediately(NakedObject object) throws ObjectPerstsistenceException {
        log("Resolve immediately: " + object);
        decorated.resolveImmediately(object);
    }

    public void runTransaction(PersistenceCommand[] commands) throws ObjectPerstsistenceException {
        log("Run transactions");
        for (int i = 0; i < commands.length; i++) {
            log("  " + i + " " + commands[i]);
        }
        decorated.runTransaction(commands);
    }

    public void shutdown() throws ObjectPerstsistenceException {
        log("Shutting down " + decorated);
        decorated.shutdown();
        close();
    }

    public void startTransaction() throws ObjectPerstsistenceException {
        log("Start transaction");
        decorated.startTransaction();
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