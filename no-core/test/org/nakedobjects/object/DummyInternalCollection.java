package org.nakedobjects.object;
import org.nakedobjects.object.control.Permission;

import java.util.Enumeration;

public class DummyInternalCollection implements InternalCollection {

    public NakedObjectSpecification getElementSpecification() {
        return null;
    }

    public NakedObject forParent() {
        return null;
    }

    public void addAll(NakedCollection coll) {}

    public void add(NakedObject object) {}

    public void added(NakedObject object) {}

    public Permission canAdd(NakedObject object) {
        return null;
    }

    public Permission canRemove(NakedObject object) {
        return null;
    }

    public boolean contains(NakedObject object) {
        return false;
    }

    public Enumeration elements() {
        return null;
    }

    public void remove(NakedObject element) {}

    public void removeAll() {}

    public void removed(NakedObject element) {}

    public int size() {
        return 0;
    }

    public boolean isEmpty() {
        return false;
    }

    public NakedObject elementAt(int index) {
        return null;
    }

    public void created() {}

    public void deleted() {}

    public String getIconName() {
        return null;
    }

    public Oid getOid() {
        return null;
    }

    public boolean isResolved() {
        return false;
    }

    public void setOid(Oid oid) {}

    public void setResolved() {}

    public NakedObjectContext getContext() {
        return null;
    }

    public void setContext(NakedObjectContext context) {}

    public void copyObject(Naked object) {}

    public NakedObjectSpecification getSpecification() {
        return null;
    }

    public boolean isSameAs(Naked object) {
        return false;
    }

    public String titleString() {
        return null;
    }

    public Enumeration oids() {
        return null;
    }

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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