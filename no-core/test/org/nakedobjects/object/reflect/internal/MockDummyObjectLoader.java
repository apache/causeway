package org.nakedobjects.object.reflect.internal;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.persistence.Oid;

import java.util.Enumeration;

public class MockDummyObjectLoader implements NakedObjectLoader {
    private NakedObject adapter;

    public void shutdown() {}

    public void reset() {}

    public Enumeration dirtyObjects() {
        return null;
    }

    public String getDebugData() {
        return null;
    }

    public String getDebugTitle() {
        return null;
    }

    public void setupAdapter(NakedObject adapter) {
        this.adapter = adapter;
    }


    public NakedObject createAdapterForTransient(Object object) {
        return adapter;
    }

    public Enumeration getIdentifiedObjects() {
        return null;
    }

    public NakedCollection createCollectionAdapter(Object collection) {
        return null;
    }

    public NakedObject createTransientInstance(NakedObjectSpecification specification) {
        return adapter;
    }

    public Naked recreateExistingInstance(NakedObjectSpecification specification) {
        return null;
    }

    public NakedObject createInstance(NakedObjectSpecification specification) {
        return null;
    }

    public NakedObject recreateAdapter(Oid oid, NakedObjectSpecification spec) {
        return null;
    }

    public NakedValue createAdapterForValue(Object value) {
        return null;
    }

    public NakedObject getAdapterFor(Oid oid) {
        return null;
    }

    public NakedObject getAdapterFor(Object object) {
        return null;
    }

    public boolean isIdentityKnown(Oid oid) {
        return false;
    }

    public void loading(NakedObject object, ResolveState state) {}

    public void loaded(NakedObject object, ResolveState state) {}

    public void unloaded(NakedObject object) {}

    public void makePersistent(NakedObject object, Oid oid) {}

    public NakedObject getAdapterOrCreateTransientFor(Object object) {
        return null;
    }

    public boolean needsLoading(NakedObject object) {
        return false;
    }

    public NakedValue createValueInstance(NakedObjectSpecification specification) {
        return null;
    }

    public void init() {}

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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