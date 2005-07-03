package org.nakedobjects.object.persistence;

import org.nakedobjects.object.DirtyObjectSet;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.utility.DebugInfo;
import org.nakedobjects.utility.StartupException;


public interface NakedObjectManager extends DebugInfo {
    NakedObject createAdapterForTransient(Object object);
    
    NakedValue createAdapterForValue(Object value);
    
    NakedObject getAdapterFor(Object obj);
    
    
    
    void abortTransaction();

    void addObjectChangedListener(DirtyObjectSet listener);
        
    TypedNakedCollection allInstances(NakedObjectSpecification specification, boolean includeSubclasses);

    NakedObject createPersistentInstance(NakedObjectSpecification specification);

    /**
     * A utility method for creating new objects in the context of the system -
     * that is, it is added to the pool of objects the enterprise system
     * contains.
     */
    NakedObject createPersistentInstance(String className);

    NakedObject createTransientInstance(NakedObjectSpecification specification);

    NakedObject createTransientInstance(String className);

    /**
     * Removes the specified object from the system. The specified object's data
     * should be removed from the persistence mechanism.
     */
    void destroyObject(NakedObject object);

    void endTransaction();

    TypedNakedCollection findInstances(InstancesCriteria criteria) throws UnsupportedFindException;

    NakedClass getNakedClass(NakedObjectSpecification specification);

     /**
     * Checks whether there are any instances of the specified type. The object
     * store should look for instances of the type represented by <variable>type
     * </variable> and return <code>true</code> if there are, or
     * <code>false</code> if there are not.
     */
    boolean hasInstances(NakedObjectSpecification specification);

    /**
     * Initialize the object store so that calls to this object store access
     * persisted objects and persist changes to the object that are saved.
     */
    void init() throws StartupException;

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
    void makePersistent(NakedObject object);

    /**
     * A count of the number of instances matching the specified pattern.
     */
    int numberOfInstances(NakedObjectSpecification specification);

    void objectChanged(NakedObject object);

    void reset();

    /**
     * Hint that specified field within the specified object is likely to be
     * needed soon. This allows the object's data to be loaded, ready
     * for use.
     * 
     * <p>
     * This method need not do anything, but offers the object store the
     * opportunity to load in objects before their use. Contrast this
     * with resolveImmediately, which requires an object to be loaded before
     * continuing.
     * 
     * @see #resolveImmediately(NakedObject)
     */
    void resolveLazily(NakedObject object, NakedObjectField field);

    /**
     * Re-initialises the fields of an object. If the object is unresolved then
     * the object's missing data should be retreieved from the persistence
     * mechanism and be used to set up the value objects and associations.
     */
    void resolveImmediately(NakedObject object);

    /**
     * Persists any objects whose state has changed. Essentially the data held by the
     * persistence mechanism should be updated to reflect the state of the
     * dirty objects.
     */
    void saveChanges();

    void shutdown();

    void startTransaction();

    /**
     * Creates new instance of the specified class, and creates an adapter to hold it.
     */
    Naked recreateExistingInstance(NakedObjectSpecification specification);

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