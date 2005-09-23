package org.nakedobjects.object.persistence.defaults;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.DirtyObjectSet;
import org.nakedobjects.object.DirtyObjectSetImpl;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedReference;
import org.nakedobjects.object.NullDirtyObjectSet;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.defaults.AbstractNakedObjectManager;
import org.nakedobjects.object.persistence.DestroyObjectCommand;
import org.nakedobjects.object.persistence.InstancesCriteria;
import org.nakedobjects.object.persistence.NakedObjectStore;
import org.nakedobjects.object.persistence.NotPersistableException;
import org.nakedobjects.object.persistence.ObjectManagerException;
import org.nakedobjects.object.persistence.ObjectNotFoundException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.utility.StartupException;
import org.nakedobjects.utility.ToString;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;


public class LocalObjectManager extends AbstractNakedObjectManager implements PersistedObjectAdder {
    private static final Logger LOG = Logger.getLogger(LocalObjectManager.class);
    private boolean checkObjectsForDirtyFlag;
    private final Hashtable nakedClasses = new Hashtable();
    private final DirtyObjectSetImpl objectsToBeSaved = new DirtyObjectSetImpl();
    private NakedObjectStore objectStore;
    private DirtyObjectSet objectsToRefreshViewsFor = new NullDirtyObjectSet();
    private Transaction transaction;
    private int transactionLevel;
    private PersistAlgorithm persistAlgorithm;

    public LocalObjectManager() {
        LOG.info("creating object manager");
    }

    public void abortTransaction() {
        if (transaction != null) {
            transaction.abort();
            transaction = null;
            transactionLevel = 0;
        }
    }

    public void addObjectChangedListener(DirtyObjectSet listener) {
        Assert.assertNotNull("must set a listener", listener);
        this.objectsToRefreshViewsFor = listener;
    }

    /**
     * Removes all the data from the specified object. All associations are set to nulll; values have clear()
     * call on them; and internal collections are reset so they have zero elements
     */
    private void clear(NakedObject object) {
        NakedObjectField[] fields = object.getSpecification().getFields();

        for (int i = 0; i < fields.length; i++) {
            NakedObjectField field = fields[i];

            if (field.isCollection()) {
                object.clearCollection((OneToManyAssociation) field);
            } else if (field.isValue()) {
                object.clearValue((OneToOneAssociation) field);
            } else if (field.isObject()) {
                NakedObject ref = (NakedObject) object.getField(field);
                if (ref != null) {
                    object.clearAssociation((OneToOneAssociation) field, ref);
                }
            }
        }
    }

    public void createObject(NakedObject object) throws ObjectManagerException {
        getTransaction().addCommand(objectStore.createCreateObjectCommand(object));
    }

    /**
     * Removes the specified object from the system. The specified object's data should be removed from the
     * persistence mechanism.
     */
    public void destroyObject(NakedObject object) {
        LOG.info("destroyObject " + object);

        DestroyObjectCommand command = objectStore.createDestroyObjectCommand(object);
        getTransaction().addCommand(command);
        object.destroyed();
        loader().start(object, ResolveState.UPDATING);
        clear(object);
        loader().end(object);

        // TODO need to do garbage collection instead
        // loader().unloaded(object);
    }

    private NakedObjectLoader loader() {
        return NakedObjects.getObjectLoader();
    }

    public void endTransaction() {
        transactionLevel--;
        if (transactionLevel == 0) {
            // TODO collate changes before committing
            saveChanges();
            getTransaction().commit(objectStore);
            transaction = null;
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        LOG.info("finalizing object manager");
    }

    public String getDebugData() {
        DebugString debug = new DebugString();
        debug.append(objectStore);
        return debug.toString();
    }

    public String getDebugTitle() {
        return objectStore.getDebugTitle();
    }

    protected NakedObject[] getInstances(InstancesCriteria criteria) {
        LOG.info("getInstances matching " + criteria);
        NakedObject[] instances = objectStore.getInstances(criteria);
        collateChanges();
        return instances;
    }

    protected NakedObject[] getInstances(NakedObjectSpecification specification, boolean includeSubclasses) {
        LOG.info("getInstances of " + specification.getShortName());
        NakedObject[] instances = objectStore.getInstances(specification, false);
        collateChanges();
        return instances;
    }

    public NakedClass getNakedClass(NakedObjectSpecification specification) {
        if (nakedClasses.contains(specification)) {
            return (NakedClass) nakedClasses.get(specification);
        }

        NakedClass spec;
        try {
        spec = objectStore.getNakedClass(specification.getFullName());
        } catch (ObjectNotFoundException e) {
            spec = new NakedClass(specification.getFullName());
        }
        nakedClasses.put(specification, spec);
        return spec;
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification specification) {
        Assert.assertNotNull("needs an OID", oid);
        Assert.assertNotNull("needs a specification", specification);

        NakedObject object;
        /*
         * if(oid == null) { object = NakedObjects.getObjectLoader().createTransientInstance(objectType); }
         * else
         */
        if (NakedObjects.getObjectLoader().isIdentityKnown(oid)) {
            object = NakedObjects.getObjectLoader().getAdapterFor(oid);
        } else {
            object = objectStore.getObject(oid, specification);
        }
        return object;
    }

    private Transaction getTransaction() {
        if (transaction == null) {
            throw new TransactionException("No transaction started");
        }
        return transaction;
    }

    /**
     * Checks whether there are any instances of the specified type. The object store should look for
     * instances of the type represented by <variable>type </variable> and return <code>true</code> if there
     * are, or <code>false</code> if there are not.
     */
    public boolean hasInstances(NakedObjectSpecification specification) {
        LOG.info("hasInstances of " + specification.getShortName());
        return objectStore.hasInstances(specification, false);
    }

    /**
     * Initialize the object store so that calls to this object store access persisted objects and persist
     * changes to the object that are saved.
     */
    public void init() throws StartupException {
        Assert.assertNotNull("persist algorithm required", persistAlgorithm);
        Assert.assertNotNull("object store required", objectStore);
        persistAlgorithm.init();
        objectStore.init();
    }

    private boolean isPersistent(NakedReference object) {
        return object.getOid() != null;
    }

    /**
     * Makes a naked object persistent. The specified object should be stored away via this object store's
     * persistence mechanism, and have an new and unique OID assigned to it (by calling the object's
     * <code>setOid</code> method). The object, should also be added to the cache as the object is
     * implicitly 'in use'.
     * 
     * <p>
     * If the object has any associations then each of these, where they aren't already persistent, should
     * also be made persistent by recursively calling this method.
     * </p>
     * 
     * <p>
     * If the object to be persisted is a collection, then each element of that collection, that is not
     * already persistent, should be made persistent by recursively calling this method.
     * </p>
     * 
     */
    public void makePersistent(NakedObject object) {
        if (isPersistent(object)) {
            throw new NotPersistableException("Object already persistent");
        }
        if (object.persistable() == Persistable.TRANSIENT) {
            throw new NotPersistableException("Object must be transient");
        }

        persistAlgorithm.makePersistent(object, this);
    }

    /*
     * private void persist(NakedObject object) { if(object.getResolveState().isPersistent() ||
     * object.getSpecification().persistable() == Persistable.TRANSIENT) { return; }
     * 
     * LOG.info("persist " + object); loader().madePersistent(object, createOid(object));
     * 
     * 
     * NakedObjectField[] fields = object.getFields(); for (int i = 0; i < fields.length; i++) {
     * NakedObjectField field = fields[i]; if (field.isDerived()) { continue; } else if (field.isValue()) {
     * continue; } else if (field instanceof OneToManyAssociation) { InternalCollection collection =
     * (InternalCollection) object.getField(field); collection.setOid(createOid(collection));
     * collection.setResolved(); for (int j = 0; j < collection.size(); j++) {
     * persist(collection.elementAt(j)); } } else { Object fieldValue = object.getField(field); if (fieldValue ==
     * null) { continue; } if (!(fieldValue instanceof NakedObject)) { throw new
     * NakedObjectRuntimeException(); } persist((NakedObject) fieldValue); } }
     * 
     * try { createObject(object); } catch (ObjectStoreException e) { throw new
     * NakedObjectRuntimeException(e); } }
     */

    /**
     * A count of the number of instances matching the specified pattern.
     */
    public int numberOfInstances(NakedObjectSpecification specification) {
        LOG.info("numberOfInstances like " + specification.getShortName());
        return objectStore.numberOfInstances(specification, false);
    }

    public void objectChanged(NakedObject object) {
        if (!object.getResolveState().isIgnoreChanges()) {
            objectsToBeSaved.addDirty(object);
            objectsToRefreshViewsFor.addDirty(object);
        }
    }

    public void reset() {
        objectStore.reset();
    }

    public void resolveField(NakedObject object, NakedObjectField field) {
        if (field.isValue()) {
            return;
        }
        NakedReference reference = (NakedReference) object.getField(field);
        if (reference.getResolveState().isResolved()) {
            return;
        }
        if (!reference.getResolveState().isPersistent()) {
            return;
        }

        LOG.info("resolve-field" + object + "/" + field.getName());
        objectStore.resolveField(object, field);
    }

    public void reload(NakedObject object) {}

    public void resolveImmediately(NakedObject object) {
        ResolveState resolveState = object.getResolveState();
        if (resolveState.isResolvable(ResolveState.RESOLVING)) {
            Assert.assertFalse("only resolve object that is not yet resolved", object, object.getResolveState().isResolved());
            Assert.assertTrue("only resolve object that is persistent", object, object.getResolveState().isPersistent());

            LOG.info("resolve-immediately: " + object);
            objectStore.resolveImmediately(object);
        }
    }

    public void saveChanges() {
        LOG.info("saving changed objects");
        collateChanges();
        Enumeration e = objectsToBeSaved.dirtyObjects();
        while (e.hasMoreElements()) {
            NakedObject object = (NakedObject) e.nextElement();
            LOG.debug("  changed " + object);
            if (isPersistent(object)) {
                getTransaction().addCommand(objectStore.createSaveObjectCommand(object));
            }
        }
    }

    private synchronized void collateChanges() {
        if (checkObjectsForDirtyFlag) {
            LOG.debug("collating changed objects");
            Enumeration e = loader().getIdentifiedObjects();
            while (e.hasMoreElements()) {
                Object o = e.nextElement();
                if (o instanceof NakedObject) {
                    NakedObject object = (NakedObject) o;
                    if (object.getSpecification().isDirty(object)) {
                        LOG.debug("  found dirty object " + object);
                        objectChanged(object);
                        object.getSpecification().clearDirty(object);
                    }
                }
            }
        }
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_CheckObjectsForDirtyFlag(boolean checkObjectsForDirtyFlag) {
        this.checkObjectsForDirtyFlag = checkObjectsForDirtyFlag;
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_ObjectStore(NakedObjectStore objectStore) {
        this.objectStore = objectStore;
    }

    public void setCheckObjectsForDirtyFlag(boolean checkObjectsForDirtyFlag) {
        this.checkObjectsForDirtyFlag = checkObjectsForDirtyFlag;
    }

    public void setObjectStore(NakedObjectStore objectStore) {
        this.objectStore = objectStore;
    }

    public void shutdown() {
        LOG.info("shutting down " + this);
        if (transaction != null) {
            try {
                abortTransaction();
            } catch (Exception e2) {
                LOG.error("failure during abort", e2);
            }
        }
        objectsToBeSaved.shutdown();
        objectsToRefreshViewsFor.shutdown();
        objectStore.shutdown();
        objectStore = null;
        nakedClasses.clear();
    }

    public void startTransaction() {
        if (transaction == null) {
            transaction = new Transaction();
            transactionLevel = 0;
        }
        transactionLevel++;
    }

    public String toString() {
        ToString toString = new ToString(this);
        if (objectStore != null) {
            toString.append("objectStore", objectStore.name());
        }
        if (persistAlgorithm != null) {
            toString.append("oidGenerator", persistAlgorithm.name());
        }
        return toString.toString();
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_PersistAlgorithm(PersistAlgorithm persistAlgorithm) {
        this.persistAlgorithm = persistAlgorithm;
    }

    public void setPersistAlgorithm(PersistAlgorithm persistAlgorithm) {
        this.persistAlgorithm = persistAlgorithm;
    }

    public void tempResetDirty() {
        objectsToBeSaved.dirtyObjects();
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
