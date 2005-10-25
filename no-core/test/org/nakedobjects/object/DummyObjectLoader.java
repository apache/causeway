package org.nakedobjects.object;

import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.DummyNakedCollection;
import org.nakedobjects.object.reflect.DummyNakedObject;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import junit.framework.Assert;


public class DummyObjectLoader implements NakedObjectLoader {
    private Hashtable identites = new Hashtable();
    private Hashtable objectAdapters = new Hashtable();
    private Hashtable collectionAdapters = new Hashtable();
    private Hashtable valueAdapters = new Hashtable();
    private Hashtable recreatedPersistent = new Hashtable();
    private Vector recreatedTransient = new Vector();

    public void addAdapter(Object object, NakedObject nakedobject) {
        objectAdapters.put(object, nakedobject);
    }

    public void addAdapter(Object object, NakedValue value) {
        valueAdapters.put(object, value);
    }

    public void addAdapter(Object object, NakedCollection collection) {
        collectionAdapters.put(object, collection);
    }

    public void addIdentity(Oid oid, NakedReference adapter) {
        identites.put(oid, adapter);
    }

    public void addRecreated(Oid oid, NakedReference adapter) {
        recreatedPersistent.put(oid, adapter);
    }
    public void addRecreatedTransient(NakedReference adapter) {
        recreatedTransient.addElement(adapter);
    }

    public NakedCollection createAdapterForCollection(Object collection, NakedObjectSpecification specification) {
        throw new NakedObjectRuntimeException();
    }

    public NakedObject createAdapterForTransient(Object object) {
        DummyNakedObject no = new DummyNakedObject();
        no.setupObject(object);
        return no;
    }

    public NakedValue createAdapterForValue(Object value) {
        return (NakedValue) valueAdapters.get(value);
    }

    public NakedObject createTransientInstance(NakedObjectSpecification specification) {
        throw new NakedObjectRuntimeException();
    }

    public NakedValue createValueInstance(NakedObjectSpecification specification) {
        throw new NakedObjectRuntimeException();
    }

    public void end(NakedReference object) {
        ResolveState finalState = object.getResolveState().getEndState();
        ((DummyNakedObject) object).setupResolveState(finalState);
    }

    public NakedObject getAdapterFor(Object object) {
        throw new NakedObjectRuntimeException();
    }

    public NakedObject getAdapterFor(Oid oid) {
        NakedObject no = (NakedObject) identites.get(oid);
        if (no == null) {
            throw new DummyException("No object for oid: " + oid);
        }
        return no;
    }

    public NakedCollection getAdapterForElseCreateAdapterForCollection(
            NakedObject parent,
            String fieldName,
            NakedObjectSpecification elementSpecification,
            Object collection) {
        NakedCollection adapter = (NakedCollection) collectionAdapters.get(collection);
        if(adapter ==  null) {
            throw new DummyException("No adapter for collection: " + collection);
        }
        return adapter;
    }

    public NakedObject getAdapterForElseCreateAdapterForTransient(Object object) {
        NakedObject adapter = (NakedObject) objectAdapters.get(object);
        if(adapter ==  null) {
            throw new DummyException("No adapter for object: " + object);
        }
        return adapter;
    }

    public String getDebugData() {
        return null;
    }

    public String getDebugTitle() {
        return null;
    }

    public Enumeration getIdentifiedObjects() {
        throw new NakedObjectRuntimeException();
    }

    public void init() {}

    public boolean isIdentityKnown(Oid oid) {
        return identites.containsKey(oid);
    }

    public void madePersistent(NakedReference object, Oid oid) {
        Assert.assertFalse(identites.containsKey(oid));
        ((DummyNakedObject) object).setupOid(oid);
        identites.put(oid, object);
    }

    public NakedObject recreateAdapterForPersistent(Oid oid, NakedObjectSpecification spec) {
        DummyNakedObject nakedObject = (DummyNakedObject) recreatedPersistent.get(oid);
        if (nakedObject == null) {
            throw new DummyException("No adapter for " + oid);
        }
        nakedObject.setupOid(oid);
        return nakedObject;
    }

    public NakedObject recreateAdapterForPersistent(Oid oid, Object object) {
        DummyNakedObject nakedObject = (DummyNakedObject) recreatedPersistent.get(oid);
        if (nakedObject == null) {
            throw new DummyException("No adapter for " + oid);
        }
        nakedObject.setupOid(oid);
        return nakedObject;
    }
    
    public NakedCollection recreateCollection(NakedObjectSpecification specification) {
        return new DummyNakedCollection();
    }

    public NakedObject recreateTransientInstance(NakedObjectSpecification specification) {
        NakedObject element = (NakedObject) recreatedTransient.elementAt(0);
        recreatedTransient.removeElementAt(0);
        return element;
    }

    public void reset() {}

    public void shutdown() {}

    public void start(NakedReference object, ResolveState targetState) {
        ((DummyNakedObject) object).setupResolveState(targetState);
    }

    public void unloaded(NakedObject object) {}

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
