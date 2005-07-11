package org.nakedobjects.object;

import org.nakedobjects.object.persistence.Oid;

import java.util.Enumeration;

public class MockObjectLoader implements NakedObjectLoader {

    public NakedObject createAdapterForTransient(Object object) {
        return null;
    }

    public NakedValue createAdapterForValue(Object value) {
        return null;
    }

    public NakedCollection createCollectionAdapter(Object collection) {
        return null;
    }

    public NakedObject createTransientInstance(NakedObjectSpecification specification) {
        return null;
    }

    public NakedValue createValueInstance(NakedObjectSpecification specification) {
        return null;
    }

    public NakedObject getAdapterFor(Object object) {
        return null;
    }

    public NakedObject getAdapterFor(Oid oid) {
        return null;
    }

    public NakedObject getAdapterForElseCreateAdapterForTransient(Object object) {
        return null;
    }

    public Enumeration getIdentifiedObjects() {
        return null;
    }

    public boolean isIdentityKnown(Oid oid) {
        return false;
    }

    public void loaded(NakedObject object, ResolveState state) {}

    public void loading(NakedObject object, ResolveState state) {}

    public void madePersistent(NakedObject object, Oid oid) {}

    public boolean canBeLoaded(NakedObject object, ResolveState newState) {
        return false;
    }

    public NakedObject recreateAdapterForPersistent(Oid oid, NakedObjectSpecification spec) {
        return null;
    }

    public void reset() {}

    public void unloaded(NakedObject object) {}

    public void init() {}

    public void shutdown() {}

    public String getDebugData() {
        return null;
    }

    public String getDebugTitle() {
        return null;
    }

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