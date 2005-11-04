package org.nakedobjects.object.defaults;

import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectPersistenceManager;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.UnsupportedFindException;


public abstract class AbstracObjectPersistenceManager implements NakedObjectPersistenceManager {
    public abstract void abortTransaction();

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

     /**
     * Creates an new instance of the class specified in the specification and
     * creates a NakedObject adapter for it.
     */
    public NakedObject createTransientInstance(NakedObjectSpecification specification) {
        NakedObject adapter = NakedObjects.getObjectLoader().createTransientInstance(specification);
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
