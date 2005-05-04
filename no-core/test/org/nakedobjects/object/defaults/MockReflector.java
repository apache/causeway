package org.nakedobjects.object.defaults;

import org.nakedobjects.object.MockOneToOneAssociation;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.FieldPeer;
import org.nakedobjects.object.reflect.ObjectTitle;
import org.nakedobjects.object.reflect.Reflector;


public class MockReflector implements Reflector {
    private NakedObject acquireInstance;
    NakedObjectSpecificationImpl superClass;

    public MockReflector() {
        super();
    }

    public Naked acquireInstance() {
        return acquireInstance;
    }

    public ActionPeer[] actionPeers(boolean forClass) {
        return new ActionPeer[0];
    }

    public String[] actionSortOrder() {
        return new String[0];
    }

    public String[] classActionSortOrder() {
        return new String[0];
    }

    public Hint classHint() {
        return null;
    }

    public void clearDirty(NakedObject object2) {}

    public FieldPeer[] fields() {
        return new FieldPeer[] { new MockOneToOneAssociation() };
    }

    public String[] fieldSortOrder() {
        return new String[0];
    }

    public String fullName() {
        return "fullname";
    }

    public String[] getInterfaces() {
        return new String[0];
    }

    public String getSuperclass() {
        return superClass.toString();
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isDirty(NakedObject object) {
        return false;
    }

    public boolean isLookup() {
        return false;
    }

    public boolean isObject() {
        return false;
    }

    public boolean isPartOf() {
        return false;
    }

    public boolean isValue() {
        return false;
    }

    public void markDirty(NakedObject object2) {}

    public Persistable persistable() {
        return null;
    }

    public String pluralName() {
        return "plural";
    }

    public void setupAcquireInstance(NakedObject object) {
        acquireInstance = object;
    }

    public String shortName() {
        return "short";
    }

    public String singularName() {
        return "singular";
    }

    public ObjectTitle title() {
        return null;
    }

    public String unresolvedTitle(NakedObject pojo) {
        return null;
    }

    public Object getExtension(Class cls) {
        return null;
    }
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