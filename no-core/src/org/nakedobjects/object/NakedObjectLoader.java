package org.nakedobjects.object;

import org.nakedobjects.NakedObjectsComponent;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.utility.DebugInfo;

import java.util.Enumeration;


/**
 * The NakedObjectLoader is responsible for managing the adapters and identities for each and every
 * POJO that is being used by the NOF. It provides a consistent set of adapters in memory, providing
 * adapter for the POJOs that are in use by the NOF and ensuring that the same object is not loaded
 * twice into memory.
 * 
 * Each POJO is given an adapted so that the NOF can work with the POJOs even though it does not
 * understand their types. Each POJO maps to an adapter and these are reused
 * 
 * Loading of an object refers to the initializing of state within each object as it is restored for
 * persistent storage.
 */
public interface NakedObjectLoader extends NakedObjectsComponent, DebugInfo {
    /**
     * Creates an adpater for the specified transient object, and add that adapter to Pojo-adapter
     * map. If the specified object object already exists in the map then the previously generated
     * adapter will be returned instead of creating one.
     */
    NakedObject createAdapterForTransient(Object object);

    /**
     * Creates an adpater for the specified value object. Values are not cached so each call to this
     * will always create new adapter.
     */
    NakedValue createAdapterForValue(Object value);

    /**
     * Creates an adpater for the specified collection.
     */
    NakedCollection createCollectionAdapter(final Object collection);

    /**
     * Creates a new transient instance of the type declared by the specification, and creates an
     * adapter for it. During this process any 'logical' object creation requirements should be
     * satisfied, i.e. its logical constructor shoud be called, not just its normal constructor.
     * Contrast this with
     * {@link NakedObjectLoader#recreateTransientInstance(NakedObjectSpecification)}.
     * 
     * @see #createAdapterForTransient(Object)
     */
    NakedObject createTransientInstance(NakedObjectSpecification specification);

    /**
     * Creates value instance of the type declared by the specification, and creates an adapter for
     * it.
     * 
     * @see #createAdapterForValue(Object)
     */
    NakedValue createValueInstance(NakedObjectSpecification specification);

    /**
     * Marks the specified object as loaded: resolved, partly resolve or updated as specified by the
     * second parameter. Attempting to specify any other state throws a runtime exception.
     */
    //  void loaded(NakedObject object, ResolveState state);
    void end(NakedObject object);

    /**
     * Retrieves an existing adapter, from the Pojo-adapter map, for the specified object. If the
     * object is not in the map then null is returned.
     */
    NakedObject getAdapterFor(Object object);

    /**
     * Retrieves an existing adapter, from the identity-adapter map, for the specified object. If
     * the OID is not in the map then null is returned.
     */
    NakedObject getAdapterFor(Oid oid);

    /**
     * Retrieves an existing adapter, from the Pojo-adapter map, for the specified object. If the
     * object is not in the map then a new adapter is returned.
     * 
     * @see #createAdapterForTransient(Object)
     */
    NakedObject getAdapterForElseCreateAdapterForTransient(Object object);

    /**
     * Returns an enumeration of all the adapters in the identity-adapter map.
     */
    Enumeration getIdentifiedObjects();

    /**
     * Returns true if the object for the specified OID exists, ie it is already loaded.
     */
    boolean isIdentityKnown(Oid oid);

    /**
     * Marks the specified adapter as perstient (as opposed to to being transient) and sets the OID
     * on the adapter. The adapter is added to the identity-adapter map.
     */
    void madePersistent(NakedObject object, Oid oid);

    /**
     * Recreates an adapter for a persistent business object that is being loaded into the system. If
     * an adapter already exists for the specified OID then that adapter is returned. Otherwise a
     * new instance of the specified business object is created and an adapter is created for it.
     * The adapter will then be in the state UNRESOLVED.
     */
    NakedObject recreateAdapterForPersistent(Oid oid, NakedObjectSpecification spec);

    /**
     * Recreates a new collection instance - normally within another machine - of the type declared
     * by the specification, and creates an adapter for it.
     * 
     * @see #createCollectionAdapter(Object)
     */
    NakedCollection recreateCollection(NakedObjectSpecification specification);

    /**
     * Recreates a new transient instance - normally within another machine - of the type declared
     * by the specification, and creates an adapter for it. During this process the 'logical' object
     * creation requirements will not be satisfied, only its constructor will be called. Contrast
     * this with {@link #createTransientInstance(NakedObjectSpecification)}.
     * 
     * @see #createAdapterForTransient(Object)
     */
    NakedObject recreateTransientInstance(NakedObjectSpecification specification);

    /**
     * Resets the loader to a known state.
     */
    void reset();

    /**
     * Marks the specified object as being loaded: resolving, partly resolving or updating as
     * specified by the second parameter. Attempting to specify any other state throws a runtime
     * exception.
     */
    //  void loading(NakedObject object, ResolveState state);
    void start(NakedObject object, ResolveState targetState);

    /**
     * Unloads the specified object from both the identity-adapter map, and the pojo-adapter map.
     * This indicates that the object is no longer in use, and therefore that no objects exists
     * within the system.
     */
    void unloaded(NakedObject object);

    //    void serializing(NakedObject object);
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