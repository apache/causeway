package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.persistence.Oid;

import java.util.Enumeration;


public class DummyNakedCollection implements NakedCollection {
    public boolean contains(NakedObject object) {
        return false;
    }

    public NakedObject elementAt(int index) {
        return null;
    }

    public Enumeration elements() {
        return new Enumeration() {
            public boolean hasMoreElements() {
                return false;
            }

            public Object nextElement() {
                return null;
            }
        };
    }

    public Enumeration oids() {
        return null;
    }

    public int size() {
        return 0;
    }

    public String getIconName() {
        return null;
    }

    public Object getObject() {
        return null;
    }

    public Oid getOid() {
        return null;
    }

    public NakedObjectSpecification getSpecification() {
        return null;
    }

    public String titleString() {
        return null;
    }

    public Hint getHint(NakedObjectField field, Naked value) {
        return null;
    }

    public long getVersion() {
        return 0;
    }

    public void setOid(Oid oid) {}

    public void setResolved() {}

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */