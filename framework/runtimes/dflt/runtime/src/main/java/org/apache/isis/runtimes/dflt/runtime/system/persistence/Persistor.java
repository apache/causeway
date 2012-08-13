package org.apache.isis.runtimes.dflt.runtime.system.persistence;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.query.Query;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.runtime.persistence.UnsupportedFindException;
import org.apache.isis.runtimes.dflt.runtime.persistence.internal.RuntimeContextFromSession;

/**
 * Represents the client-side API of the <tt>PersistenceSession</tt>.
 */
public interface Persistor {

    
    /////////////////////////////////////////////////////////////////
    // AdapterManager
    /////////////////////////////////////////////////////////////////

    /**
     * Access to looking up (and possibly lazily loading) adapters.
     * 
     * <p>
     * However, manipulating of adapters is not part of this interface.
     * @return
     */
    public abstract AdapterManager getAdapterManager();

    
    /////////////////////////////////////////////////////////////////
    // find, load, resolve
    /////////////////////////////////////////////////////////////////
    

    /**
     * Finds and returns instances that match the specified query.
     * 
     * <p>
     * The {@link QueryCardinality} determines whether all instances or just the
     * first matching instance is returned.
     * 
     * @throws UnsupportedFindException
     *             if the criteria is not support by this persistor
     */
    public abstract <T> ObjectAdapter findInstances(Query<T> query, QueryCardinality cardinality);

    /**
     * Whether there are any instances of the specified
     * {@link ObjectSpecification type}.
     *
     * <p>
     * Checks whether there are any instances of the specified type. The object
     * store should look for instances of the type represented by <variable>type
     * </variable> and return <code>true</code> if there are, or
     * <code>false</code> if there are not.
     * 
     * <p>
     * Used (ostensibly) by client-side code.
     */
    public abstract boolean hasInstances(ObjectSpecification specification);

    /**
     * Finds and returns instances that match the specified
     * {@link PersistenceQuery}.
     * 
     * <p>
     * Compared to {@link #findInstances(Query, QueryCardinality)}, not that
     * there is no {@link QueryCardinality} parameter. That's because
     * {@link PersistenceQuery} intrinsically carry the knowledge as to how many
     * rows they return.
     * 
     * @throws UnsupportedFindException
     *             if the criteria is not support by this persistor
     */
    public abstract ObjectAdapter findInstances(PersistenceQuery persistenceQuery);

    /**
     * Loads the object identified by the specified {@link TypedOid} from the
     * persisted set of objects.
     */
    public abstract ObjectAdapter loadObject(TypedOid oid);
    

    /**
     * Re-initialises the fields of an object. If the object is unresolved then
     * the object's missing data should be retrieved from the persistence
     * mechanism and be used to set up the value objects and associations.
     */
    public abstract void resolveImmediately(ObjectAdapter adapter);

    /**
     * Hint that specified field within the specified object is likely to be
     * needed soon. This allows the object's data to be loaded, ready for use.
     * 
     * <p>
     * This method need not do anything, but offers the object store the
     * opportunity to load in objects before their use. Contrast this with
     * resolveImmediately, which requires an object to be loaded before
     * continuing.
     * 
     * @see #resolveImmediately(ObjectAdapter)
     */
    public abstract void resolveField(ObjectAdapter objectAdapter, ObjectAssociation field);


    
    /////////////////////////////////////////////////////////////////
    // create, persist
    /////////////////////////////////////////////////////////////////

    /**
     * Create a root or standalone {@link ObjectAdapter adapter}.
     * 
     * <p>
     * Creates a new instance of the specified type and returns it in an adapter
     * whose resolved state set to {@link ResolveState#TRANSIENT} (except if the
     * type is marked as {@link ObjectSpecification#isValueOrIsParented()
     * aggregated} in which case it will be set to {@link ResolveState#VALUE}).
     * 
     * <p>
     * The returned object will be initialised (had the relevant callback
     * lifecycle methods invoked).
     * 
     * <p>
     * <b><i> REVIEW: not sure about {@link ResolveState#VALUE} - see comments
     * in {@link #adapterFor(Object, Oid, OneToManyAssociation)}.</i></b>
     * <p>
     * TODO: this is the same as
     * {@link RuntimeContextFromSession#createTransientInstance(ObjectSpecification)};
     * could it be unified?
     * 
     * <p>
     * While creating the object it will be initialised with default values and
     * its created lifecycle method (its logical constructor) will be invoked.
     * Contrast this with
     * {@link #recreateTransientInstance(Oid, ObjectSpecification)}.
     * 
     * <p>
     * This method is ultimately delegated to by the
     * {@link DomainObjectContainer}.
     */
    public abstract ObjectAdapter createInstance(ObjectSpecification objectSpec);

    /**
     * Creates a new instance of the specified type and returns an adapter with
     * an aggregated OID that show that this new object belongs to the specified
     * parent. The new object's resolved state is set to
     * {@link ResolveState#RESOLVED} as it state is part of it parent.
     * 
     * <p>
     * While creating the object it will be initialised with default values and
     * its created lifecycle method (its logical constructor) will be invoked.
     * Contrast this with
     * {@link #recreateTransientInstance(Oid, ObjectSpecification)}.
     * 
     * <p>
     * This method is ultimately delegated to by the
     * {@link DomainObjectContainer}.
     */
    public abstract ObjectAdapter createInstance(ObjectSpecification objectSpec, ObjectAdapter parentAdapter);

    /**
     * Makes an {@link ObjectAdapter} persistent. The specified object should be
     * stored away via this object store's persistence mechanism, and have an
     * new and unique OID assigned to it. The object, should also be added to
     * the {@link AdapterManagerSpi} as the object is implicitly 'in use'.
     * 
     * <p>
     * If the object has any associations then each of these, where they aren't
     * already persistent, should also be made persistent by recursively calling
     * this method.
     * 
     * <p>
     * If the object to be persisted is a collection, then each element of that
     * collection, that is not already persistent, should be made persistent by
     * recursively calling this method.
     * 
     * @see #remapAsPersistent(ObjectAdapter)
     */
    public abstract void makePersistent(ObjectAdapter adapter);


    /////////////////////////////////////////////////////////////////
    // change
    /////////////////////////////////////////////////////////////////

    /**
     * Mark the {@link ObjectAdapter} as changed, and therefore requiring
     * flushing to the persistence mechanism.
     */
    public abstract void objectChanged(ObjectAdapter adapter);

    
    /////////////////////////////////////////////////////////////////
    // destroy
    /////////////////////////////////////////////////////////////////

    /**
     * Removes the specified object from the system. The specified object's data
     * should be removed from the persistence mechanism.
     */
    public abstract void destroyObject(ObjectAdapter adapter);


    

}