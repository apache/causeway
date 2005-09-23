package org.nakedobjects.object.persistence;

import org.nakedobjects.object.DirtyObjectSet;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.utility.Logger;
import org.nakedobjects.utility.StartupException;


public class ObjectManagerLogger extends Logger implements NakedObjectManager {
    private final NakedObjectManager decorated;

    public ObjectManagerLogger(final NakedObjectManager decorated, final String logFileName) {
        super(logFileName, false);
        this.decorated = decorated;
    }

    public ObjectManagerLogger(final NakedObjectManager decorated) {
        super(null, false);
        this.decorated = decorated;
    }

    public void abortTransaction() {
        log("Abort transaction");
        decorated.abortTransaction();
    }

    public void addObjectChangedListener(DirtyObjectSet listener) {
        log("Adding object changed listener " + listener);
        decorated.addObjectChangedListener(listener);
    }

    public TypedNakedCollection allInstances(NakedObjectSpecification specification, boolean includeSubclasses) {
        log("All instances of " + specification.getShortName() + (includeSubclasses ? " including subclasses" : ""));
        return decorated.allInstances(specification, includeSubclasses);
    }

    public NakedObject createPersistentInstance(NakedObjectSpecification specification) {
        NakedObject instance = decorated.createPersistentInstance(specification);
        log("Create an instances of " + specification.getShortName(), instance.getObject());
        return instance;
    }

    public NakedObject createPersistentInstance(String className) {
        NakedObject instance = decorated.createPersistentInstance(className);
        log("Create an instances of " + className, instance.getObject());
        return instance;
    }

    public NakedObject createTransientInstance(NakedObjectSpecification specification) {
        NakedObject instance = decorated.createTransientInstance(specification);
        log("Create a transient instances of " + specification.getShortName(), instance.getObject());
        return instance;
    }

    public NakedObject createTransientInstance(String className) {
        NakedObject instance = decorated.createTransientInstance(className);
        log("Create a transient instances of " + className, instance.getObject());
        return instance;
    }

    public void destroyObject(NakedObject object) {
        log("Destroy " + object.getObject());
        decorated.destroyObject(object);
    }

    public void endTransaction() {
        log("End transaction");
        decorated.endTransaction();
    }

    public TypedNakedCollection findInstances(InstancesCriteria criteria) throws UnsupportedFindException {
        log("Find instances matching " + criteria);
        return decorated.findInstances(criteria);
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

    public NakedClass getNakedClass(NakedObjectSpecification specification) {
        NakedClass cls = decorated.getNakedClass(specification);
        log("Get class " + specification.getShortName(), cls);
        return cls;
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException {
        NakedObject object = decorated.getObject(oid, hint);
        log("Get object for " + oid + " (of type " + hint.getShortName() + ")", object.getObject());
        return object;
    }

    public boolean hasInstances(NakedObjectSpecification specification) {
        boolean hasInstances = decorated.hasInstances(specification);
        log("Has instances of " + specification.getShortName(), "" + hasInstances);
        return hasInstances;
    }

    public void init() throws StartupException {
        log("Initialising " + decorated);
        decorated.init();
    }

    public void makePersistent(NakedObject object) {
        log("Make object graph persistent: " + object);
        decorated.makePersistent(object);
    }

    public int numberOfInstances(NakedObjectSpecification specification) {
        int number = decorated.numberOfInstances(specification);
        log("Number of instances of " + specification.getShortName(), "" + number);
        return number;
    }

    public void objectChanged(NakedObject object) {
        log("object changed " + object);
        decorated.objectChanged(object);
    }

    public void reload(NakedObject object) {
        decorated.reload(object);
        log("Relead: " + object);
    }

    public void reset() {
        log("reset object manager");
        decorated.reset();
    }

    public void resolveImmediately(NakedObject object) {
        decorated.resolveImmediately(object);
        log("Resolve immediately: " + object);
    }

    public void resolveField(NakedObject object, NakedObjectField field) {
        log("Resolve eagerly object in field " + field + " of " + object);
        decorated.resolveField(object, field);
    }

    public void saveChanges() {
        log("Saving changes");
        decorated.saveChanges();
    }

    public void shutdown() {
        log("Shutting down " + decorated);
        decorated.shutdown();
        close();
    }

    public void startTransaction() {
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