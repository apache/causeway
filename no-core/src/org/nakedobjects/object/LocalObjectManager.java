package org.nakedobjects.object;

import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.utility.ComponentException;
import org.nakedobjects.utility.ComponentLoader;
import org.nakedobjects.utility.ConfigurationException;
import org.nakedobjects.utility.StartupException;

import java.util.Vector;

import org.apache.log4j.Logger;


public class LocalObjectManager extends NakedObjectManager {
    private static final Logger LOG = Logger.getLogger(LocalObjectManager.class);
    private UpdateNotifier notifier;
    private NakedObjectStore objectStore;
    private OidGenerator oidGenerator;

    public LocalObjectManager(NakedObjectStore objectStore, UpdateNotifier notifier) throws ConfigurationException,
            ComponentException {
        this.objectStore = objectStore;
        this.notifier = notifier;
        oidGenerator = (OidGenerator) ComponentLoader.loadComponent("oidgenerator", SimpleOidGenerator.class, OidGenerator.class);
        new LocalNakedClassManager(objectStore);
    }

    public void abortTransaction() {
    //objectStore.abortTransaction();
    }
    
    /**
     * Removes all the data from the specified object. All associations are set to nulll; values
     * have clear() call on them; and internal collections are reset so they have zero elements
     */
    private void clear(NakedObject object) {
        Field[] fields = object.getNakedClass().getFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];

            if (field instanceof OneToManyAssociation) {
                InternalCollection coll = (InternalCollection) field.get(object);
                coll.removeAll();
            } else if (field instanceof OneToOneAssociation) {
                NakedObject ref = (NakedObject) field.get(object);

                if (ref != null) {
                    ((OneToOneAssociation) field).clearAssociation(object, ref);
                }
            } else {
                ((NakedValue) field.get(object)).clear();
            }
        }
    }

    public final Object createOid(NakedObject object) {
        Object oid = oidGenerator.next(object);
        LOG.debug("createOid " + oid);

        return oid;
    }

    /**
     * Removes the specified object from the system. The specified object's data should be removed
     * from the persistence mechanism.
     */
    public void destroyObject(NakedObject object) {
        LOG.debug("destroyObject " + object);

        try {
            objectStore.destroyObject(object);
            object.deleted();
            clear(object);

            if (objectStore.getLoadedObjects().isLoaded(object.getOid())) {
                objectStore.getLoadedObjects().unloaded(object);
            }
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    public void endTransaction() {
        try {
            objectStore.endTransaction();
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    public Vector getInstances(NakedClass cls) {
        LOG.debug("getInstances of " + cls);
        try {
            return objectStore.getInstances(cls, false);
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    public Vector getInstances(NakedClass cls, String term) throws UnsupportedFindException {
        LOG.debug("getInstances of " + cls + " with term " + term);
        try {
            return objectStore.getInstances(cls, term, false);
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }


    /**
     * Gets the instances that match the specified pattern. The object store should create a vector
     * and add to it those instances held by the persistence mechanism that:-
     * 
     * <para>1) are of the type that the pattern object is; </para>
     * 
     * <para>2) have the same content as the pattern object where the pattern object has values or
     * references specified, i.e. empty value objects and <code>null</code> references are to be
     * ignored; </para>
     * 
     * @throws UnsupportedFindException
     */
    public Vector getInstances(NakedObject pattern) throws UnsupportedFindException {
        LOG.debug("getInstances like " + pattern);
        try {
            return objectStore.getInstances(pattern, false);
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }

    }

    /**
     * Retrieves the object identified by the specified OID from the object store. The cache should
     * be checked first and, if the object is cached, the cached version should be returned. It is
     * important that if this method is called again, while the originally returned object is in
     * working memory, then this method must return that same Java object.
     * 
     * <para>Assuming that the object is not cached then the data for the object should be retreived
     * from the persistence mechanism and the object recreated (as describe previously). The
     * specified OID should then be assigned to the recreated object by calling its <method>setOID
     * </method>. Before returning the object its resolved flag should also be set by calling its
     * <method>setResolved </method> method as well. </para>
     * 
     * <para>If the persistence mechanism does not known of an object with the specified OID then a
     * <class>ObjectNotFoundException </class> should be thrown. </para>
     * 
     * <para>Note that the OID could be for an internal collection, and is therefore related to the
     * parent object (using a <class>CompositeOid </class>). The elements for an internal collection
     * are commonly stored as part of the parent object, so to get element the parent object needs
     * to be retrieved first, and the internal collection can be got from that. </para>
     * 
     * <para>Returns the stored NakedObject object that has the specified OID. </para>
     * 
     * @return the requested naked object
     * @param oid
     *                   of the object to be retrieved
     * @throws ObjectNotFoundException
     *                    when no object corresponding to the oid can be found
     */
    public NakedObject getObject(Object oid, NakedClass hint) {
        LOG.debug("getObject " + oid);
        try {
            if (objectStore.getLoadedObjects().isLoaded(oid)) { 
                return objectStore.getLoadedObjects().getLoadedObject(oid);
            }
            return objectStore.getObject(oid, hint);
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }

    }

    /** @deprecated provided to make the method in AbstractNakedObject work */
    public NakedObjectStore getObjectStore() {
        return objectStore;
    }

    /**
     * Checks whether there are any instances of the specified type. The object store should look
     * for instances of the type represented by <variable>type </variable> and return
     * <code>true</code> if there are, or <code>false</code> if there are not.
     */
    public boolean hasInstances(NakedClass cls) {
        LOG.debug("hasInstances of " + cls);
        try {
            cls.resolve();
            return objectStore.hasInstances(cls, false);
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    /**
     * Initialize the object store so that calls to this object store access persisted objects and
     * persist changes to the object that are saved.
     */
    public void init() throws StartupException {
        try {
            oidGenerator.init();
            objectStore.init();
            NakedClassManager.getInstance().init();
        } catch (ObjectStoreException e) {
            throw new StartupException(e);
        }
    }

    /**
     * Makes a naked object persistent. The specified object should be stored away via this object
     * store's persistence mechanism, and have an new and unique OID assigned to it (by calling the
     * object's <code>setOid</code> method). The object, should also be added to the cache as the
     * object is implicitly 'in use'.
     * 
     * <p>
     * If the object has any associations then each of these, where they aren't already persistent,
     * should also be made persistent by recursively calling this method.
     * </p>
     * 
     * <p>
     * If the object to be persisted is a collection, then each element of that collection, that is
     * not already persistent, should be made persistent by recursively calling this method.
     * </p>
     *  
     */
    public void makePersistent(NakedObject object) {
        LOG.debug("makePersistent " + object);

        if (object.isPersistent()) { throw new IllegalArgumentException(); }

        object.setOid(createOid(object));

        if (!object.isResolved()) {
            object.setResolved();
        }

        Field[] fields = object.getNakedClass().getFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];

            if (field.isDerived()) {
                continue;
            } else if (field.isValue()) {
                continue;
            } else if (field.isPart()) {
                InternalCollection collection = (InternalCollection) field.get(object);
                collection.setOid(createOid(collection));
                collection.setResolved();

                for (int j = 0; j < collection.size(); j++) {
                    NakedObject element = collection.elementAt(j);

                    if (element.isPersistent()) {
                        continue;
                    }

                    makePersistent(element);
                }
            } else {
                Object fieldValue = field.get(object);

                if (fieldValue == null) {
                    continue;
                }

                NakedObject association = (NakedObject) fieldValue;

                if (association.isPersistent()) {
                    continue;
                }

                makePersistent(association);
            }
        }

        try {
            createObject(object);
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
        objectStore.getLoadedObjects().loaded(object);
    }

    protected void createObject(NakedObject object) throws ObjectStoreException {
        objectStore.createObject(object);
    }

    /**
     * A count of the number of instances matching the specified pattern.
     */
    public int numberOfInstances(NakedClass cls) {
        LOG.debug("numberOfInstances like " + cls);
        try {
            return objectStore.numberOfInstances(cls, false);
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }

    }

    /**
     * Persists the specified object's state. Essentially the data held by the persistence mechanism
     * should be updated to reflect the state of the specified objects. Once updated, the object
     * store should issue a notification to all of the object's users via the <class>UpdateNotifier
     * </class> object. This can be achieved simply, if extending the <class>AbstractObjectStore
     * </class> by calling its <method>broadcastObjectUpdate </method> method.
     */
    public void objectChanged(NakedObject object) {
        LOG.debug("objectChanged " + object);
        try {
            objectStore.save(object);
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }

        if (object.isPersistent()) {
            LOG.debug("broadcastObjectUpdate " + object);
            notifier.broadcastObjectChanged(object, this);
        }
    }

    /**
     * Re-initialises the fields of an object. This method should return immediately if the object's
     * resolved flag (determined by calling <method>isResolved </method> on the object) is already
     * set. If the object is unresolved then the object's missing data should be retreieved from the
     * persistence mechanism and be used to set up the value objects and associations. The object
     * should be set up in the same manner as in <method>getObject </method> above.
     */
    public void resolve(NakedObject object) {
        if (object.isResolved()) { return; }

        LOG.info("resolve " + object);

        if (!objectStore.getLoadedObjects().isLoaded(object.getOid())) {
            objectStore.getLoadedObjects().loaded(object);
        }
        try {
            objectStore.resolve(object);
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
        object.setResolved();
    }

    /**
     * Generates a unique serial number for the specified squence set. Each set of serial numbers
     * are a simple numerical sequence. Calling this method with a unused sequence name creates a
     * new set.
     */
    public long serialNumber(String sequence) {
        LOG.debug("serialNumber " + sequence);

        try {
            Vector instances = objectStore.getInstances(NakedClassManager.getInstance().getNakedClass(Sequence.class.getName()), false);
            Sequence number;

            for (int i = 0; i < instances.size(); i++) {
                number = (Sequence) instances.elementAt(i);
                if (number.getName().isSameAs(sequence)) {
                    number = (Sequence) instances.elementAt(i);
                    number.getSerialNumber().next();
                    objectStore.save(number);
                    return number.getSerialNumber().longValue();
                }
            }

            number = new Sequence();
            number.created();
            number.getName().setValue(sequence);
            makePersistent(number);
            return number.getSerialNumber().longValue();
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    public void shutdown() {
        try {
            oidGenerator.shutdown();
            objectStore.shutdown();
            NakedClassManager.getInstance().shutdown();
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
        super.shutdown();
    }

    public void startTransaction() {
        try {
            objectStore.startTransaction();
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }

    }

    public String toString() {
        return "LocalObjectManager [objectStore=" + objectStore.name() + ",oidGenerator=" + oidGenerator.name() + "]";
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
