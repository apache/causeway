package org.nakedobjects.object;

import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.OneToOnePeer;
import org.nakedobjects.object.security.Session;

public class MockOneToOneAssociation implements OneToOnePeer {

    public MockOneToOneAssociation() {
        super();
    }

    public boolean hasHint() {
        return false;
    }

    public String getName() {
        return null;
    }

    public void clearAssociation(NakedObject inObject, NakedObject associate) {}

    public Hint getHint(Session session, NakedObject object, Naked value) {
        return null;
    }

    public Naked getAssociation(NakedObject inObject) {
        return null;
    }

    public NakedObjectSpecification getType() {
        return null;
    }

    public void initValue(NakedObject inObject, Object associate) {}

    public boolean isDerived() {
        return false;
    }

    public void setAssociation(NakedObject inObject, NakedObject associate) {}

    public void parseTextEntry(NakedObject inObject, String text) throws TextEntryParseException, InvalidEntryException {}

    public boolean isEmpty(NakedObject inObject) {
        return false;
    }

    public void setValue(NakedObject inObject, Object associate) {}

    public void initAssociation(NakedObject inObject, NakedObject associate) {}

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