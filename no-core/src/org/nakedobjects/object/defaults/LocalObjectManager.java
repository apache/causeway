package org.nakedobjects.object.defaults;

import org.nakedobjects.container.configuration.ComponentException;
import org.nakedobjects.container.configuration.ConfigurationException;
import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.NotPersistableException;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.OidGenerator;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.UpdateNotifier;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.reflect.OneToManyAssociationSpecification;
import org.nakedobjects.object.reflect.OneToOneAssociationSpecification;
import org.nakedobjects.utility.StartupException;

import java.util.Hashtable;

import org.apache.log4j.Logger;


public class LocalObjectManager extends AbstractNakedObjectManager {
    private static final Logger LOG = Logger.getLogger(LocalObjectManager.class);
    private NakedObjectContext context;

    private final Hashtable nakedClasses = new Hashtable();
    private UpdateNotifier notifier;
    private NakedObjectStore objectStore;
    private OidGenerator oidGenerator;

    public LocalObjectManager(NakedObjectStore objectStore, UpdateNotifier notifier, OidGenerator oidGenerator)
            throws ConfigurationException, ComponentException {
        this.objectStore = objectStore;
        this.notifier = notifier;
        this.oidGenerator = oidGenerator;
        context = new NakedObjectContext(this);
    }

    public void abortTransaction() {
        try {
            objectStore.abortTransaction();
        } catch (ObjectStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Removes all the data from the specified object. All associations are set
     * to nulll; values have clear() call on them; and internal collections are
     * reset so they have zero elements
     */
    private void clear(NakedObject object) {
        FieldSpecification[] fields = object.getSpecification().getFields();

        for (int i = 0; i < fields.length; i++) {
            FieldSpecification field = fields[i];

            if (field instanceof OneToManyAssociationSpecification) {
                InternalCollection coll = (InternalCollection) field.get(object);
                coll.removeAll();
            } else if (field instanceof OneToOneAssociationSpecification) {
                NakedObject ref = (NakedObject) field.get(object);

                if (ref != null) {
                    ((OneToOneAssociationSpecification) field).clearAssociation(object, ref);
                }
            } else {
                ((NakedValue) field.get(object)).clear();
            }
        }
    }

    private void createNakedClassSpec(NakedObject object) throws ObjectStoreException {
        objectStore.createNakedClass((NakedClass) object);
    }

    private void createObject(NakedObject object) throws ObjectStoreException {
        objectStore.createObject(object);
    }

    public final Oid createOid(NakedObject object) {
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

    protected NakedObjectContext getContext() {
        return context;
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
            setInstancesContext(instances);
            return instances;
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }

    }

    protected NakedObject[] getInstances(NakedObjectSpecification cls, boolean includeSubclasses) {
        LOG.debug("getInstances of " + cls);
        try {
            NakedObject[] instances = objectStore.getInstances(cls, false);
            setInstancesContext(instances);
            return instances;
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }
    
    protected NakedObject[] getInstances(InstancesCriteria criteria, boolean includeSubclasses) {
        LOG.debug("getInstances matching " + criteria);
        try {
            NakedObject[] instances = objectStore.getInstances(criteria, false);
            setInstancesContext(instances);
            return instances;
        }  catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    protected NakedObject[] getInstances(NakedObjectSpecification cls, String term, boolean includeSubclasses) throws UnsupportedFindException {
        LOG.debug("getInstances of " + cls + " with term " + term);
        try {
            NakedObject[] instances = objectStore.getInstances(cls, term, false);
            setInstancesContext(instances);
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
            spec = new SimpleNakedClass(nakedClass.getFullName());
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
        nakedClasses.put(nakedClass, spec);
        spec.setContext(getContext());
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
            if (objectStore.getLoadedObjects().isLoaded(oid)) {
                return objectStore.getLoadedObjects().getLoadedObject(oid);
            }
            NakedObject object = objectStore.getObject(oid, hint);
            object.setContext(context);
            return object;
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }

    }

    /** @deprecated provided to make the method in AbstractNakedObject work */
    public NakedObjectStore getObjectStore() {
        return objectStore;
    }

    /**
     * Checks whether there are any instances of the specified type. The object
     * store should look for instances of the type represented by <variable>type
     * </variable> and return <code>true</code> if there are, or
     * <code>false</code> if there are not.
     */
    public boolean hasInstances(NakedObjectSpecification cls) {
        LOG.debug("hasInstances of " + cls);
        try {
            return objectStore.hasInstances(cls, false);
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

        FieldSpecification[] fields = object.getSpecification().getFields();

        for (int i = 0; i < fields.length; i++) {
            FieldSpecification field = fields[i];

            if (field.isDerived()) {
                continue;
            } else if (field.isValue()) {
                continue;
//            } else if (field.isPart()) {
            } else if (field instanceof OneToManyAssociationSpecification) {
                InternalCollection collection = (InternalCollection) field.get(object);
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
                Object fieldValue = field.get(object);

                if (fieldValue == null) {
                    continue;
                }

                NakedObject association = (NakedObject) fieldValue;

                if (isPersistent(association)) {
                    continue;
                }

                makePersistent(association);
            }
        }

        try {
            if (object instanceof NakedClass) {
                createNakedClassSpec(object);
            } else {
                createObject(object);
            }
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
        objectStore.getLoadedObjects().loaded(object);
    }

    /**
     * A count of the number of instances matching the specified pattern.
     */
    public int numberOfInstances(NakedObjectSpecification cls) {
        LOG.debug("numberOfInstances like " + cls);
        try {
            return objectStore.numberOfInstances(cls, false);
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }

    }

    /**
     * Persists the specified object's state. Essentially the data held by the
     * persistence mechanism should be updated to reflect the state of the
     * specified objects. Once updated, the object store should issue a
     * notification to all of the object's users via the <class>UpdateNotifier
     * </class> object. This can be achieved simply, if extending the
     * <class>AbstractObjectStore </class> by calling its
     * <method>broadcastObjectUpdate </method> method.
     */
    public void objectChanged(NakedObject object) {
        LOG.debug("objectChanged " + object);
        if (isPersistent(object)) {
            try {
                objectStore.save(object);
            } catch (ObjectStoreException e) {
                throw new NakedObjectRuntimeException(e);
            }
        }

        LOG.debug("broadcastObjectUpdate " + object);
        notifier.broadcastObjectChanged(object, this);
    }

    /**
     * Re-initialises the fields of an object. This method should return
     * immediately if the object's resolved flag (determined by calling
     * <method>isResolved </method> on the object) is already set. If the object
     * is unresolved then the object's missing data should be retreieved from
     * the persistence mechanism and be used to set up the value objects and
     * associations. The object should be set up in the same manner as in
     * <method>getObject </method> above.
     */
    public void resolve(NakedObject object) {
        if (object.isResolved() || !isPersistent(object)) {
            return;
        }

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
     * Generates a unique serial number for the specified squence set. Each set
     * of serial numbers are a simple numerical sequence. Calling this method
     * with a unused sequence name creates a new set.
     */
    public long serialNumber(String sequence) {
        LOG.debug("serialNumber " + sequence);

        try {
            NakedObject[] instances = objectStore.getInstances(NakedObjectSpecificationLoader.getInstance().loadSpecification(
                    Sequence.class.getName()), false);
            Sequence number;

            for (int i = 0, len = instances.length; i < len; i++) {
                number = (Sequence) instances[i];
                if (number.getName().isSameAs(sequence)) {
                    number = (Sequence) instances[i];
                    number.getSerialNumber().next();
                    objectStore.save(number);
                    return number.getSerialNumber().longValue();
                }
            }

            number = new Sequence();
            number.setNakedClass(NakedObjectSpecificationLoader.getInstance().loadSpecification(Sequence.class));
            number.created();
            number.getName().setValue(sequence);
            makePersistent(number);
            return number.getSerialNumber().longValue();
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    private void setInstancesContext(NakedObject[] instances) {
        for (int i = 0, len = instances.length; i < len; i++) {
            instances[i].setContext(context);
        }
    }

    public void shutdown() {
        try {
            oidGenerator.shutdown();
            objectStore.shutdown();
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
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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
