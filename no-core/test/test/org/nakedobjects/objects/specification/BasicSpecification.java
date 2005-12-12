package test.org.nakedobjects.objects.specification;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.Action.Type;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.utility.NakedObjectRuntimeException;

abstract class BasicSpecification implements NakedObjectSpecification {
    protected NakedObjectField[] fields;

    public void addSubclass(NakedObjectSpecification specification) {}

    public void clearDirty(NakedObject object) {}

    public void deleted(NakedObject object) {}

    public NakedObjectField[] getAccessibleFields() {
        return fields;
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

    public Object getExtension(Class cls) {
        return null;
    }

    public Class[] getExtensions() {
        return new Class[0];
    }

    public NakedObjectField getField(String name) {
        for (int i = 0; i < fields.length; i++) {
            if(fields[i].getId().equals(name)) {
                return fields[i];
            }
        }
        throw new NakedObjectRuntimeException("Field not found: " + name);
    }

    public Object getFieldExtension(String name, Class cls) {
        return null;
    }

    public Class[] getFieldExtensions(String name) {
        return new Class[0];
    }

    public NakedObjectField[] getFields() {
        return fields;
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

    public NakedObjectField[] getVisibleFields(NakedObject object) {
        return fields;
    }

    public boolean hasSubclasses() {
        return false;
    }

    public NakedObjectSpecification[] interfaces() {
        return new NakedObjectSpecification[0];
    }

    public void introspect() {}

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
        return false;
    }

    public boolean isOfType(NakedObjectSpecification specification) {
        return this == specification;
    }

    public boolean isValue() {
        return false;
    }

    public void markDirty(NakedObject object) {}

    public Persistable persistable() {
        return Persistable.USER_PERSISTABLE;
    }

    public NakedObjectSpecification[] subclasses() {
        return new NakedObjectSpecification[0];
    }

    public NakedObjectSpecification superclass() {
        return null;
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