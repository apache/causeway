package org.nakedobjects.object.defaults;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ObjectFactory;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.defaults.collection.InstanceCollectionVector;
import org.nakedobjects.object.persistence.InstancesCriteria;
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
    
    public TypedNakedCollection allInstances(NakedObjectSpecification specification, boolean includeSubclasses) {
        NakedObject[] instances = getInstances(specification, includeSubclasses);
        TypedNakedCollection collection = new InstanceCollectionVector(specification, instances);
        return collection;
    }

    public NakedObject createInstance(NakedObjectSpecification specification) {
       Object object = objectFactory.createObject(specification);
       NakedObject nakedObject = NakedObjects.getPojoAdapterFactory().createNOAdapter(object);
       makePersistent(nakedObject);
       return nakedObject;
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

    public TypedNakedCollection findInstances(InstancesCriteria criteria)
            throws UnsupportedFindException {
        NakedObject[] instances = getInstances(criteria);
        NakedObjectSpecification specification = criteria.getSpecification();
        TypedNakedCollection collection = new InstanceCollectionVector(specification, instances);
        return collection;
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

    protected abstract NakedObject[] getInstances(NakedObjectSpecification cls, boolean includeSubclasses);

    protected abstract NakedObject[] getInstances(InstancesCriteria criteria);
    
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
