package org.nakedobjects.object;

import org.nakedobjects.object.collection.SimpleInstanceCollection;
import org.nakedobjects.object.collection.TypedNakedCollection;
import org.nakedobjects.utility.DebugInfo;

import org.apache.log4j.Logger;


public abstract class AbstractNakedObjectManager implements DebugInfo, NakedObjectManager {
    private static final Logger LOG = Logger.getLogger(AbstractNakedObjectManager.class);

    public abstract void abortTransaction();

     public abstract Oid createOid(NakedObject object);

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

    public NakedObject getObject(NakedObject object) throws ObjectNotFoundException {
        return getObject(object.getOid(), object.getSpecification());
    }
    
    /** @deprecated provided to make the method in AbstractNakedObject work */
    public abstract NakedObjectStore getObjectStore();

    public void shutdown() {
    }

     public TypedNakedCollection allInstances(String className) {
        NakedObjectSpecification cls = NakedObjectSpecification.getNakedClass(className);
        return allInstances(cls);
    }

    protected abstract NakedObjectContext getContext();
    
    public TypedNakedCollection allInstances(NakedObjectSpecification nakedClass) {
        NakedObject[] instances = getInstances(nakedClass);
        TypedNakedCollection collection = new SimpleInstanceCollection(nakedClass, instances);
        collection.setContext(getContext());
        return  collection;
    }

    public TypedNakedCollection findInstances(String className, String searchTerm) throws UnsupportedFindException {
        NakedObjectSpecification cls = NakedObjectSpecification.getNakedClass(className);
        return findInstances(cls, searchTerm);
    }

    public TypedNakedCollection findInstances(NakedObjectSpecification nakedClass, String searchTerm) {
        NakedObject[] instances = getInstances(nakedClass, searchTerm);
        TypedNakedCollection collection = new SimpleInstanceCollection(nakedClass, instances);
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

    public TypedNakedCollection findInstances(NakedObject pattern) {
        NakedObjectSpecification nakedClass = pattern.getSpecification();
        NakedObject[] instances = getInstances(pattern);
        TypedNakedCollection collection = new SimpleInstanceCollection(nakedClass, instances);
        collection.setContext(getContext());
        return  collection;
   }
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
