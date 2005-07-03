package org.nakedobjects.object.defaults;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ObjectFactory;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.defaults.collection.InstanceCollectionVector;
import org.nakedobjects.object.persistence.InstancesCriteria;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.UnsupportedFindException;
import org.nakedobjects.object.reflect.PojoAdapter;
import org.nakedobjects.object.reflect.PojoAdapterFactoryImpl;

import org.apache.log4j.Logger;


public abstract class AbstractNakedObjectManager extends PojoAdapterFactoryImpl implements NakedObjectManager {
    private static final Logger LOG = Logger.getLogger(AbstractNakedObjectManager.class);
    protected ObjectFactory objectFactory;

    public abstract void abortTransaction();

    public void debugCheckObjectForOid(Oid oid, NakedObject object) {}

    public AbstractNakedObjectManager() {}

    public AbstractNakedObjectManager(final ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_ObjectFactory(ObjectFactory objectFactory) {
        setObjectFactory(objectFactory);
    }

    public TypedNakedCollection allInstances(NakedObjectSpecification specification, boolean includeSubclasses) {
        NakedObject[] instances = getInstances(specification, includeSubclasses);
        TypedNakedCollection collection = new InstanceCollectionVector(specification, instances);
        return collection;
    }

    /**
     * Creates an new instance of the class specified in the specification and
     * creates a NakedObject adapter for it. This new object is then made
     * persistent
     */
    public NakedObject createPersistentInstance(NakedObjectSpecification specification) {
        NakedObject adapter = createTransientInstance(specification);
        makePersistent(adapter);
        return adapter;
    }

    /**
     * Creates an new instance of the class specified and creates a NakedObject
     * adapter for it. This new object is then made persistent
     */
    public NakedObject createPersistentInstance(String className) {
        NakedObjectSpecification cls = NakedObjects.getSpecificationLoader().loadSpecification(className);
        return createPersistentInstance(cls);
    }

    protected abstract Oid createOid(Naked object);

    /**
     * Creates an new instance of the class specified in the specification and
     * creates a NakedObject adapter for it.
     */
    public NakedObject createTransientInstance(NakedObjectSpecification specification) {
        Object object = objectFactory.createNewLogicalObject(specification);
        NakedObject adapter = createAdapterForTransient(object);
        ((PojoAdapter) adapter).setTransient();
        return adapter;
    }

    /**
     * Creates an new instance of the class specified and creates a NakedObject
     * adapter for it.
     */
    public NakedObject createTransientInstance(String className) {
        NakedObjectSpecification nc = NakedObjects.getSpecificationLoader().loadSpecification(className);
        return createTransientInstance(nc);
    }

    public Naked recreateExistingInstance(NakedObjectSpecification specification) {
        Object object = objectFactory.recreateObject(specification);
        NakedObject adapter = createNOAdapter(object);
        return adapter;
    }

    public TypedNakedCollection findInstances(InstancesCriteria criteria) throws UnsupportedFindException {
        if (criteria == null) {
            throw new NullPointerException();
        }
        NakedObject[] instances = getInstances(criteria);
        NakedObjectSpecification specification = criteria.getSpecification();
        TypedNakedCollection collection = new InstanceCollectionVector(specification, instances);
        return collection;
    }

    protected abstract NakedObject[] getInstances(NakedObjectSpecification cls, boolean includeSubclasses);

    protected abstract NakedObject[] getInstances(InstancesCriteria criteria);

    public void shutdown() {
        objectFactory = null;
    }

    /**
     * Recreates an adapter for a persistent business object that is being
     * loaded into the system. If an adapter already exists for the specified
     * OID then that adapter is returned. Otherwise a new instance of the
     * specified business object is created and an adapter is created for it.
     * The adapter will then be in the state UNRESOLVED.
     */
    public NakedObject recreateAdapter(Oid oid, NakedObjectSpecification specification) {
        if (isIdentityKnown(oid)) {
            return getAdapterFor(oid);
        }

        LOG.debug("recreating object " + specification.getFullName() + "/" + oid);
        Object object = objectFactory.recreateObject(specification);
        PojoAdapter adapter = (PojoAdapter) createAdapterForPersistent(object, oid);

        adapter.recreate(oid);
        return adapter;
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
