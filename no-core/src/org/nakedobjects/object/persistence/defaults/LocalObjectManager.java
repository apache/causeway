package org.nakedobjects.object.persistence.defaults;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.DirtyObjectSet;
import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.defaults.AbstractNakedObjectManager;
import org.nakedobjects.object.persistence.DestroyObjectCommand;
import org.nakedobjects.object.persistence.NakedObjectStore;
import org.nakedobjects.object.persistence.NotPersistableException;
import org.nakedobjects.object.persistence.ObjectNotFoundException;
import org.nakedobjects.object.persistence.ObjectStoreException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.object.persistence.UnsupportedFindException;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.PojoAdapterFactory;
import org.nakedobjects.utility.NotImplementedException;
import org.nakedobjects.utility.StartupException;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;


public class LocalObjectManager extends AbstractNakedObjectManager {
    private static final Logger LOG = Logger.getLogger(LocalObjectManager.class);
    private boolean checkObjectsForDirtyFlag;
    private final Hashtable nakedClasses = new Hashtable();
    private DirtyObjectSet objectsToBeSaved = new DirtyObjectSet();
    private NakedObjectStore objectStore;
    private DirtyObjectSet objectsToRefreshViewsFor;
    private OidGenerator oidGenerator;
    private Transaction transaction;

    public LocalObjectManager() {
        LOG.info("creating object manager");
    }

    public void abortTransaction() {
        try {
            getTransaction().abort();
            objectStore.abortTransaction();
        } catch (ObjectStoreException e) {
            e.printStackTrace();
        }
    }

    public void addObjectChangedListener(DirtyObjectSet listener) {
        this.objectsToRefreshViewsFor = listener;
    }

    /**
     * Removes all the data from the specified object. All associations are set
     * to nulll; values have clear() call on them; and internal collections are
     * reset so they have zero elements
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

    private void createNakedClassSpec(NakedObject object) throws ObjectStoreException {
        throw new NotImplementedException();
        //        objectStore.createNakedClass(object);
    }

    private void createObject(NakedObject object) throws ObjectStoreException {
        getTransaction().addCommand(objectStore.createCreateObjectCommand(object));
    }

    public final Oid createOid(Naked object) {
        Oid oid = oidGenerator.next(object);
        LOG.debug("createOid " + oid);
        return oid;
    }

    /**
     * Removes the specified object from the system. The specified object's data
     * should be removed from the persistence mechanism.
     */
    public void destroyObject(NakedObject object) {
        LOG.debug("destroyObject " + object);

        DestroyObjectCommand command = objectStore.createDestroyObjectCommand(object);
        getTransaction().addCommand(command);
        object.deleted();
        clear(object);

        //      if (NakedObjects.getPojoAdapterFactory().isLoaded(object.getOid())) {
        NakedObjects.getPojoAdapterFactory().unloaded(object);
        //      }
    }

    public void endTransaction() {
        try {
            getTransaction().commit(objectStore);
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        LOG.info("finalizing object manager");
    }

    public String getDebugData() {
        StringBuffer data = new StringBuffer();
        data.append('\n');
        data.append(objectStore.getDebugData());
        data.append('\n');
        return data.toString();
    }

    public String getDebugTitle() {
        return objectStore.getDebugTitle();
    }

    protected NakedObject[] getInstances(InstancesCriteria criteria, boolean includeSubclasses) {
        LOG.debug("getInstances matching " + criteria);
        try {
            NakedObject[] instances = objectStore.getInstances(criteria, false);
            return instances;
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    /**
     * Gets the instances that match the specified pattern. The object store
     * should create a vector and add to it those instances held by the
     * persistence mechanism that:-
     * 
     * <para>1) are of the type that the pattern object is; </para>
     * 
     * <para>2) have the same content as the pattern object where the pattern
     * object has values or references specified, i.e. empty value objects and
     * <code>null</code> references are to be ignored; </para>
     * 
     * @throws UnsupportedFindException
     */
    protected NakedObject[] getInstances(NakedObject pattern, boolean includeSubclasses) throws UnsupportedFindException {
        LOG.debug("getInstances like " + pattern);
        try {
            NakedObject[] instances = objectStore.getInstances(pattern, false);
            return instances;
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }

    }

    protected NakedObject[] getInstances(NakedObjectSpecification specification, boolean includeSubclasses) {
        LOG.debug("getInstances of " + specification);
        try {
            NakedObject[] instances = objectStore.getInstances(specification, false);
            return instances;
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    protected NakedObject[] getInstances(NakedObjectSpecification specification, String term, boolean includeSubclasses)
            throws UnsupportedFindException {
        LOG.debug("getInstances of " + specification + " with term " + term);
        try {
            NakedObject[] instances = objectStore.getInstances(specification, term, false);
            return instances;
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    public NakedClass getNakedClass(NakedObjectSpecification nakedClass) {
        if (nakedClasses.contains(nakedClass)) {
            return (NakedClass) nakedClasses.get(nakedClass);
        }

        NakedClass spec;
        try {
            spec = objectStore.getNakedClass(nakedClass.getFullName());
        } catch (ObjectNotFoundException e) {
            spec = new NakedClass(nakedClass.getFullName());
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
        nakedClasses.put(nakedClass, spec);
        //        spec.setContext(getContext());
        return spec;
    }

    /**
     * Retrieves the object identified by the specified OID from the object
     * store. The cache should be checked first and, if the object is cached,
     * the cached version should be returned. It is important that if this
     * method is called again, while the originally returned object is in
     * working memory, then this method must return that same Java object.
     * 
     * <para>Assuming that the object is not cached then the data for the object
     * should be retreived from the persistence mechanism and the object
     * recreated (as describe previously). The specified OID should then be
     * assigned to the recreated object by calling its <method>setOID </method>.
     * Before returning the object its resolved flag should also be set by
     * calling its <method>setResolved </method> method as well. </para>
     * 
     * <para>If the persistence mechanism does not known of an object with the
     * specified OID then a <class>ObjectNotFoundException </class> should be
     * thrown. </para>
     * 
     * <para>Note that the OID could be for an internal collection, and is
     * therefore related to the parent object (using a <class>CompositeOid
     * </class>). The elements for an internal collection are commonly stored as
     * part of the parent object, so to get element the parent object needs to
     * be retrieved first, and the internal collection can be got from that.
     * </para>
     * 
     * <para>Returns the stored NakedObject object that has the specified OID.
     * </para>
     * 
     * @return the requested naked object
     * @param oid
     *                       of the object to be retrieved
     */
    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) {
        LOG.debug("getObject " + oid);
        try {
            if (NakedObjects.getPojoAdapterFactory().isLoaded(oid)) {
                return NakedObjects.getPojoAdapterFactory().getLoadedObject(oid);
            }
            NakedObject object = objectStore.getObject(oid, hint);
            return object;
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    private Transaction getTransaction() {
        if (transaction == null) {
            throw new TransactionException("No transaction started");
        }
        return transaction;
    }

    /**
     * Checks whether there are any instances of the specified type. The object
     * store should look for instances of the type represented by <variable>type
     * </variable> and return <code>true</code> if there are, or
     * <code>false</code> if there are not.
     */
    public boolean hasInstances(NakedObjectSpecification specification) {
        LOG.debug("hasInstances of " + specification);
        try {
            return objectStore.hasInstances(specification, false);
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    /**
     * Initialize the object store so that calls to this object store access
     * persisted objects and persist changes to the object that are saved.
     */
    public void init() throws StartupException {
        try {
            oidGenerator.init();
            objectStore.init();
        } catch (ObjectStoreException e) {
            throw new StartupException(e);
        }
    }

    private boolean isPersistent(NakedObject object) {
        return object.getOid() != null;
    }

    private PojoAdapterFactory loadedObjects() {
        return NakedObjects.getPojoAdapterFactory();
    }

    /**
     * Makes a naked object persistent. The specified object should be stored
     * away via this object store's persistence mechanism, and have an new and
     * unique OID assigned to it (by calling the object's <code>setOid</code>
     * method). The object, should also be added to the cache as the object is
     * implicitly 'in use'.
     * 
     * <p>
     * If the object has any associations then each of these, where they aren't
     * already persistent, should also be made persistent by recursively calling
     * this method.
     * </p>
     * 
     * <p>
     * If the object to be persisted is a collection, then each element of that
     * collection, that is not already persistent, should be made persistent by
     * recursively calling this method.
     * </p>
     *  
     */
    public void makePersistent(NakedObject object) {
        LOG.debug("makePersistent " + object);

        if (isPersistent(object)) {
            throw new NotPersistableException("Object already persistent");
        }

        object.setOid(createOid(object));

        if (!object.isResolved()) {
            object.setResolved();
        }

        NakedObjectSpecification specification = object.getSpecification();
        NakedObjectField[] fields = specification.getFields();

        for (int i = 0; i < fields.length; i++) {
            NakedObjectField field = fields[i];

            if (field.isDerived()) {
                continue;
            } else if (field.isValue()) {
                continue;
            } else if (field instanceof OneToManyAssociation) {
                InternalCollection collection = (InternalCollection) object.getField(field);
                collection.setOid(createOid(collection));
                collection.setResolved();

                for (int j = 0; j < collection.size(); j++) {
                    NakedObject element = collection.elementAt(j);

                    if (isPersistent(element)) {
                        continue;
                    }

                    makePersistent(element);
                }
            } else {
                Object fieldValue = object.getField(field);

                if (fieldValue == null) {
                    continue;
                }

                if (!(fieldValue instanceof NakedObject)) {
                    throw new NakedObjectRuntimeException();
                }
                NakedObject association = (NakedObject) fieldValue;

                if (isPersistent(association)) {
                    continue;
                }

                makePersistent(association);
            }
        }

        try {
            if (object.getObject() instanceof NakedClass) {
                createNakedClassSpec(object);
            } else {
                createObject(object);
            }
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
        loadedObjects().loaded(object);
    }

    /**
     * A count of the number of instances matching the specified pattern.
     */
    public int numberOfInstances(NakedObjectSpecification specification) {
        LOG.debug("numberOfInstances like " + specification);
        try {
            return objectStore.numberOfInstances(specification, false);
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }

    }

    public void objectChanged(NakedObject object) {
        objectsToBeSaved.addDirty(object);
        if (objectsToRefreshViewsFor != null) {
            objectsToRefreshViewsFor.addDirty(object);
        }
    }

    public void reset() {
        NakedObjects.getPojoAdapterFactory().reset();
    }

    public void resolveEagerly(NakedObject object, NakedObjectField field) {
        if (object.isResolved() || !isPersistent(object)) {
            return;
        }
        LOG.info("resolve-eagerly" + object + "/" + field);
        try {
            objectStore.resolveEagerly(object, field);
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    public void resolveImmediately(NakedObject object) {
        if (object.isResolved() || !isPersistent(object)) {
            return;
        }
        LOG.info("resolve-immediately" + object);
        try {
            objectStore.resolveImmediately(object);
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
        object.setResolved();
    }

    public void saveChanges() {
        LOG.debug("collating changes");
        if (checkObjectsForDirtyFlag) {
            collateChanges();
        }
        Enumeration e = objectsToBeSaved.dirtyObjects();
        while (e.hasMoreElements()) {
            NakedObject object = (NakedObject) e.nextElement();
            LOG.debug("  changed " + object);
            if (isPersistent(object)) {
                getTransaction().addCommand(objectStore.createSaveObjectCommand(object));
            }
        }
    }

    private void collateChanges() {
        PojoAdapterFactory factory = loadedObjects();
        Enumeration e = factory.getLoadedObjects();
        while (e.hasMoreElements()) {
            NakedObject object = (NakedObject) e.nextElement();
            if (object.getSpecification().isDirty(object)) {
                objectChanged(object);
                
                object.getSpecification().clearDirty(object);
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

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_OidGenerator(OidGenerator oidGenerator) {
        this.oidGenerator = oidGenerator;
    }

    public void setCheckObjectsForDirtyFlag(boolean checkObjectsForDirtyFlag) {
        this.checkObjectsForDirtyFlag = checkObjectsForDirtyFlag;
    }

    public void setObjectStore(NakedObjectStore objectStore) {
        this.objectStore = objectStore;
    }

    public void setOidGenerator(OidGenerator oidGenerator) {
        this.oidGenerator = oidGenerator;
    }

    public void shutdown() {
        try {
            oidGenerator.shutdown();
            oidGenerator = null;
            objectStore.shutdown();
            objectStore = null;
            nakedClasses.clear();
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
        super.shutdown();
    }

    public void startTransaction() {
        transaction = new Transaction();
    }

    public String toString() {
        return "LocalObjectManager [objectStore=" + objectStore.name() + ",oidGenerator=" + oidGenerator.name() + "]";
    }
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
