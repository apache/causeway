package org.nakedobjects.object;

import org.nakedobjects.object.collection.InstanceCollection;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.utility.DebugInfo;
import org.nakedobjects.utility.StartupException;

import org.apache.log4j.Logger;


public abstract class AbstractNakedObjectManager implements DebugInfo, NakedObjectManager {
    private static final Logger LOG = Logger.getLogger(AbstractNakedObjectManager.class);

    public abstract void abortTransaction();

     public abstract Oid createOid(NakedObject object);

    /**
     * Removes the specified object from the system. The specified object's data should be
     * removed from the persistence mechanism.
     */
    public abstract void destroyObject(NakedObject object);

    public abstract void endTransaction();

    public String getDebugData() {
        StringBuffer data = new StringBuffer();
        data.append("Using object store " + getObjectStore());
        data.append('\n');
        data.append('\n');
        return data.toString();
    }

    public String getDebugTitle() {
        return "Naked Object Manager";
    }

    /**
     * Gets the instances that match the specified pattern. The object store should
     * create a vector and add to it those instances held by the persistence mechanism
     * that:-
     *
     * <para>1) are of the type that the pattern object is;</para>
     *
     * <para>2) have the same content as the pattern object where the pattern object has values or
     * references specified, i.e. empty value objects and <code>null</code> references are to be
     * ignored;</para>
     */
    protected abstract NakedObject[] getInstances(NakedObject pattern)  throws UnsupportedFindException;

    protected abstract NakedObject[] getInstances(NakedObjectSpecification cls);
   
    protected abstract NakedObject[] getInstances(NakedObjectSpecification cls, String term) throws UnsupportedFindException;


 	/**
     * Retrieves the object identified by the specified OID from the object store. The cache should
     * be checked first and, if the object is cached, the cached version should be returned. It is
     * important that if this method is called again, while the originally returned object is in
     * working memory, then this method must return that same Java object.
     *
     * <para>
     * Assuming that the object is not cached then the data for the object should be retreived from
     * the persistence mechanism and the object recreated (as describe previously). The specified
     * OID should then be assigned to the recreated object by calling its <method>setOID</method>.
     * Before returning the object its resolved flag should also be set by calling its <method>
     * setResolved</method> method as well.</para>
     *
     * <para>
     * If the persistence mechanism does not known of an object with the specified OID then a
     * <class>ObjectNotFoundException</class> should be thrown.</para>
     *
     * <para>Note that the OID could be for an internal collection, and is therefore related to
     * the parent object (using a <class>CompositeOid</class>).  The elements for an internal
     * collection are commonly stored as part of the parent object, so to get element the parent
     * object needs to be retrieved first, and the internal collection can be got from that.</para>
     *
     * <para>
     * Returns the stored NakedObject object that has the specified OID.</para>
     *
     * @return the requested naked object
     * @param oid of the object to be retrieved
      */
    public abstract NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException;

    public NakedObject getObject(NakedObject object) throws ObjectNotFoundException {
        return getObject(object.getOid(), object.getSpecification());
    }
    
    /** @deprecated provided to make the method in AbstractNakedObject work */
    public abstract NakedObjectStore getObjectStore();

    /**
     * Checks whether there are any instances of the specified type. The object store should look
     * for instances of the type represented by <variable>type</variable> and return <code>true</code>
     * if there are, or <code>false</code> if there are not.
     */
    public abstract boolean hasInstances(NakedObjectSpecification cls);

    /**
     * Initialize the object store so that calls to this object store access persisted objects
     * and persist changes to the object that are saved.
      */
    public abstract void init() throws StartupException;


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
    public abstract void makePersistent(NakedObject object);

    /**
     * A count of the number of instances matching the specified pattern.
     */
    public abstract int numberOfInstances(NakedObjectSpecification cls);

    /**
     * Persists the specified object's state. Essentially the data held by the persistence
     * mechanism should be updated to reflect the state of the specified objects. Once updated, the
     * object store should issue a notification to all of the object's users via the <class>
     * UpdateNotifier</class> object. This can be achieved simply, if extending the <class>
     * AbstractObjectStore</class> by calling its <method>broadcastObjectUpdate</method> method.
     */
    public abstract void objectChanged(NakedObject object);

    /**
     * Re-initialises the fields of an object. This method should return immediately if the
     * object's resolved flag (determined by calling <method>isResolved</method> on the object)
     * is already set. If the object is unresolved then the object's missing data should be
     * retreieved from the persistence mechanism and be used to set up the value objects and
     * associations. The object should be set up in the same manner as in <method>getObject
     * </method> above.
     */
    public abstract void resolve(NakedObject object);

    /**
     * Generates a unique serial number for the specified squence set. Each set of serial numbers
     * are a simple numerical sequence. Calling this method with a unused sequence name creates a
     * new set.
     */
    public abstract long serialNumber(String sequence);

    public void shutdown() {
 //       instance = null;
    }

    public abstract void startTransaction();

     public InstanceCollection allInstances(String className) {
        NakedObjectSpecification cls = NakedObjectSpecification.getNakedClass(className);
        return allInstances(cls);
    }

    protected abstract NakedObjectContext getContext();
    
    public InstanceCollection allInstances(NakedObjectSpecification nakedClass) {
        NakedObject[] instances = getInstances(nakedClass);
        InstanceCollection collection = new InstanceCollection(nakedClass, instances);
        collection.setContext(getContext());
        return  collection;
    }

    public InstanceCollection findInstances(String className, String searchTerm) throws UnsupportedFindException {
        NakedObjectSpecification cls = NakedObjectSpecification.getNakedClass(className);
        return findInstances(cls, searchTerm);
    }

    public InstanceCollection findInstances(NakedObjectSpecification nakedClass, String searchTerm) {
        NakedObject[] instances = getInstances(nakedClass, searchTerm);
        InstanceCollection collection = new InstanceCollection(nakedClass, instances);
        collection.setContext(getContext());
        return  collection;
    }
    
    /**
    A utility method for creating new objects in the context of the system - that is, it is added to the pool of
    objects the enterprise system contains.
    */
    public NakedObject createInstance(String className) {
        NakedObjectSpecification cls = NakedObjectSpecification.getNakedClass(className);
        return createInstance(cls);
    }
    
    public NakedObject createInstance(NakedObjectSpecification nakedClass) {
        NakedObject object;
        try {
            object = (NakedObject) nakedClass.acquireInstance();
            object.setContext(getContext());
            makePersistent(object);
            object.created();
            objectChanged(object);
        } catch (NakedObjectRuntimeException e) {
            object = new NakedError("Failed to create instance of " + nakedClass);

            LOG.error("Failed to create instance of " + nakedClass, e);
        }
        return object;
    }
    
    public NakedObject createTransientInstance(String className) {
        NakedObjectSpecification nc = NakedObjectSpecification.getNakedClass(className);
        return createTransientInstance(nc);
    }
    
    public NakedObject createTransientInstance(NakedObjectSpecification nc) {
        if (nc == null) {
            throw new RuntimeException("Invalid type to create " + nc.getFullName());
        }
        NakedObject object = (NakedObject) nc.acquireInstance();
        object.created();
        return object;
    }
    
    public InternalCollection createInternalCollection(String elementType, NakedObject forParent) {
        return new InternalCollection(elementType, forParent);
    }

    public InstanceCollection findInstances(NakedObject pattern) {
        NakedObjectSpecification nakedClass = pattern.getSpecification();
        NakedObject[] instances = getInstances(pattern);
        InstanceCollection collection = new InstanceCollection(nakedClass, instances);
        collection.setContext(getContext());
        return  collection;
   }

    public abstract NakedClass getNakedClass(NakedObjectSpecification nakedClass);


}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/
