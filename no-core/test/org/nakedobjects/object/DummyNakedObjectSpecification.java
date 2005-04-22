package org.nakedobjects.object;

import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.ObjectTitle;
import org.nakedobjects.object.reflect.PojoAdapter;
import org.nakedobjects.object.reflect.Action.Type;
import org.nakedobjects.object.security.Session;


public class DummyNakedObjectSpecification implements NakedObjectSpecification {
    private static int next = 100;
    public NakedObjectField[] fields;
    private final int id = next++;

    public DummyNakedObjectSpecification() {}

    public Naked acquireInstance() {
        return null;
    }

    public void clearDirty(NakedObject object) {}

    public NakedObject createPattern() {
        return null;
    }

    public String debugInterface() {
        return null;
    }

    public Action getClassAction(Type type, String name) {
        return null;
    }

    public Action getClassAction(Type type, String name, NakedObjectSpecification[] parameters) {
        return null;
    }

    public Action[] getClassActions(Type type) {
        return null;
    }

    public Hint getClassHint() {
        return null;
    }

    public NakedObjectField getField(String name) {
        return null;
    }

    public NakedObjectField[] getFields() {
        return fields;
    }

    public String getFullName() {
        return "DummyNakedObjectSpecification" + id;
    }

    public Action getObjectAction(Type type, String name) {
        return null;
    }

    public Action getObjectAction(Type type, String name, NakedObjectSpecification[] parameters) {
        return null;
    }

    public Action[] getObjectActions(Type type) {
        return null;
    }

    public String getPluralName() {
        return null;
    }

    public String getShortName() {
        return null;
    }

    public String getSingularName() {
        return "singular name";
    }

    public ObjectTitle getTitle() {
        return new ObjectTitle() {

            public String title(NakedObject object) {
                return "";
            }
        };
    }

    public NakedObjectField[] getVisibleFields(NakedObject object, Session session) {
        return null;
    }

    public boolean hasSubclasses() {
        return false;
    }

    public NakedObjectSpecification[] interfaces() {
        return null;
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isCollection() {
        return false;
    }

    public boolean isDirty(NakedObject object) {
        return false;
    }

    public boolean isLookup() {
        return false;
    }

    public boolean isObject() {
        return true;
    }

    public boolean isOfType(NakedObjectSpecification cls) {
        return cls == this;
    }

    public boolean isParsable() {
        return false;
    }

    public boolean isPartOf() {
        return false;
    }

    public boolean isValue() {
        return false;
    }

    public void markDirty(NakedObject object) {}

    public Persistable persistable() {
        return null;
    }

    public NakedObjectSpecification[] subclasses() {
        return null;
    }

    public NakedObjectSpecification superclass() {
        return null;
    }

    public String toString() {
        return getFullName();
    }

    public String unresolvedTitle(PojoAdapter pojo) {
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