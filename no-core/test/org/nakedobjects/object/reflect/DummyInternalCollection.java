package org.nakedobjects.object.reflect;

import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.TestVersion;
import org.nakedobjects.object.Version;
import org.nakedobjects.object.defaults.AbstractNakedReference;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.utility.Assert;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;


public class DummyInternalCollection extends AbstractNakedReference implements NakedCollection {
    private Vector collection = new Vector();
    private Oid oid;
    private NakedObjectSpecification specification;

    public DummyInternalCollection() {}

    public DummyInternalCollection(Vector vector) {
        this.collection = vector;
    }

    public void checkLock(Version version) {}

    public boolean contains(NakedObject object) {
        return collection.contains(object);
    }

    public void debugClearResolved() {}

    public void destroyed() {}

    public NakedObject elementAt(int index) {
        return (NakedObject) collection.elementAt(index);
    }

    public Enumeration elements() {
        return collection.elements();
    }

    public String getIconName() {
        return null;
    }

    public Object getObject() {
        return collection;
    }

    public Oid getOid() {
        return oid;
    }

    public ResolveState getResolveState() {
        return ResolveState.GHOST;
    }

    public NakedObjectSpecification getSpecification() {
        return specification;
    }

    public Version getVersion() {
        return new TestVersion();
    }

    public void init(Object[] initElements) {
        Assert.assertEquals("Collection not empty", 0, this.collection.size());
        for (int i = 0; i < initElements.length; i++) {
            collection.addElement(initElements[i]);
        }
    }

    public Persistable persistable() {
        return Persistable.USER_PERSISTABLE;
    }

    public void setOptimisticLock(Version version, String user, Date time) {}

    public void setupSpecification(NakedObjectSpecification specification) {
        this.specification = specification;
    }

    public int size() {
        return collection.size();
    }

    public String titleString() {
        return "title";
    }
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
