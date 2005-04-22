package org.nakedobjects.object;

import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.security.Session;

public class MockNakedValue implements NakedValue {

    private Object object;

    public MockNakedValue() {
        super();
    }

    public void parseTextEntry(String text) throws InvalidEntryException {}

    public byte[] asEncodedString() {
        return null;
    }

    public void restoreFromEncodedString(byte[] data) {}

    public int getMinumumLength() {
        return 0;
    }

    public int getMaximumLength() {
        return 0;
    }

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

    public String getIconName() {
        return null;
    }

    public Oid getOid() {
        return null;
    }

    public Object getObject() {
        return object;
    }

    public void setupObject(Object object) {
        this.object = object;
    }
    
    public void clearAssociation(NakedObjectAssociation specification, NakedObject ref) {}

    public Naked execute(Action action, Naked[] parameters) {
        return null;
    }

    public Hint getHint(Session session, Action action, Naked[] parameters) {
        return null;
    }

    public Hint getHint(Session session, NakedObjectField field, Naked value) {
        return null;
    }

    public void clearViewDirty() {}

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