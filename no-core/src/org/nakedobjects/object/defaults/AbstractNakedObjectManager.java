package org.nakedobjects.object.defaults;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ObjectFactory;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.defaults.collection.InstanceCollectionVector;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.persistence.ObjectNotFoundException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.UnsupportedFindException;


public abstract class AbstractNakedObjectManager implements NakedObjectManager {
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
	 * @property
	 */
	public void set_ObjectFactory(ObjectFactory objectFactory) {
		setObjectFactory(objectFactory);
	}

    public TypedNakedCollection allInstances(NakedObjectSpecification specification) {
        return allInstances(specification, false);
    }
    
    public TypedNakedCollection allInstances(String className) {
        return allInstances(className, false);
    }
    
    public TypedNakedCollection allInstances(NakedObjectSpecification specification, boolean includeSubclasses) {
        NakedObject[] instances = getInstances(specification, includeSubclasses);
        TypedNakedCollection collection = new InstanceCollectionVector(specification, instances);
 //       collection.setContext(getContext());
        return collection;
    }

    public TypedNakedCollection allInstances(String className, boolean includeSubclasses) {
        NakedObjectSpecification cls = NakedObjects.getSpecificationLoader().loadSpecification(className);
        return allInstances(cls, includeSubclasses);
    }

    public NakedObject createInstance(NakedObjectSpecification specification) {
       Object object = objectFactory.createObject(specification);
       NakedObject nakedObject = NakedObjects.getPojoAdapterFactory().createNOAdapter(object);
       makePersistent(nakedObject);
       return nakedObject;
       
      /* 
        NakedObject object;
        try {
            object = (NakedObject) specification.acquireInstance();
            object.setContext(getContext());
            makePersistent(object);
            object.created();
            objectChanged(object);
        } catch (NakedObjectRuntimeException e) {
            object = NakedObjects.getPojoAdapterFactory(getContext().getObjectManager().generatorError("Failed to create instance of " + specification, e));

            LOG.error("Failed to create instance of " + specification, e);
        }
        return object;
        */
    }

    /**
     * A utility method for creating new objects in the context of the system -
     * that is, it is added to the pool of objects the enterprise system
     * contains.
     */
    public NakedObject createInstance(String className) {
        NakedObjectSpecification cls = NakedObjects.getSpecificationLoader().loadSpecification(className);
        return createInstance(cls);
    }

    public abstract Oid createOid(Naked object);

    public NakedObject createTransientInstance(NakedObjectSpecification nc) {
        Object object = objectFactory.createObject(nc);
        return NakedObjects.getPojoAdapterFactory().createNOAdapter(object);
    }

    public NakedObject createTransientInstance(String className) {
        NakedObjectSpecification nc = NakedObjects.getSpecificationLoader().loadSpecification(className);
        return createTransientInstance(nc);
    }

    public TypedNakedCollection findInstances(NakedObject pattern) {
        return findInstances(pattern, false);
    }
    
    public TypedNakedCollection findInstances(NakedObject pattern, boolean includeSubclasses) {
        NakedObject[] instances = getInstances(pattern, includeSubclasses);
        NakedObjectSpecification specification = pattern.getSpecification();
        TypedNakedCollection collection = new InstanceCollectionVector(specification, instances);
        return collection;
    }

    public TypedNakedCollection findInstances(NakedObjectSpecification specification, String searchTerm) {
        return findInstances(specification, searchTerm, false);
    }
    
    public TypedNakedCollection findInstances(NakedObjectSpecification specification, String searchTerm, boolean includeSubclasses) {
        NakedObject[] instances = getInstances(specification, searchTerm, includeSubclasses);
        TypedNakedCollection collection = new InstanceCollectionVector(specification, instances);
        return collection;
    }
    
    public TypedNakedCollection findInstances(InstancesCriteria criteria) {
        return findInstances(criteria, false);
	}
    
    public TypedNakedCollection findInstances(InstancesCriteria criteria, boolean includeSubclasses)
            throws UnsupportedFindException {
        NakedObject[] instances = getInstances(criteria, includeSubclasses);
        NakedObjectSpecification specification = criteria.getSpecification();
        TypedNakedCollection collection = new InstanceCollectionVector(specification, instances);
        return collection;
    }

    public TypedNakedCollection findInstances(String className, String searchTerm) throws UnsupportedFindException {
        return findInstances(className, searchTerm, false);
    }
    
    public TypedNakedCollection findInstances(String className, String searchTerm, boolean includeSubclasses) throws UnsupportedFindException {
        NakedObjectSpecification cls = NakedObjects.getSpecificationLoader().loadSpecification(className);
        return findInstances(cls, searchTerm, includeSubclasses);
    }

    public NakedError generatorError(String message, Exception e) {
        return new Error(message, e);
    }

    public String getDebugData() {
        StringBuffer data = new StringBuffer();
        data.append('\n');
        data.append('\n');
        return data.toString();
    }

    public String getDebugTitle() {
        return "Naked Object Manager";
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
     * @param includeSubclasses TODO
     */
    protected abstract NakedObject[] getInstances(NakedObject pattern, boolean includeSubclasses) throws UnsupportedFindException;

    protected abstract NakedObject[] getInstances(NakedObjectSpecification cls, boolean includeSubclasses);

    protected abstract NakedObject[] getInstances(NakedObjectSpecification cls, String term, boolean includeSubclasses) throws UnsupportedFindException;

    protected abstract NakedObject[] getInstances(InstancesCriteria criteria, boolean includeSubclasses);
    
    public NakedObject getObject(NakedObject object) throws ObjectNotFoundException {
        return getObject(object.getOid(), object.getSpecification());
    }

    public void shutdown() {
        objectFactory = null;
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
