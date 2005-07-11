package org.nakedobjects.object.internal;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.ObjectTitle;
import org.nakedobjects.object.reflect.Action.Type;


public class InternalObjectSpecification implements NakedObjectSpecification {
    private String name;

    public InternalObjectSpecification(String name) {
        this.name = name;
    }

    public void clearDirty(NakedObject object) {}

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
        return new Action[0];
    }

    public Hint getClassHint() {
        return new DefaultHint();
    }

    public Object getExtension(Class cls) {
        return null;
    }

    public NakedObjectField getField(String name) {
        return null;
    }

    public Object getFieldExtension(String name, Class cls) {
        return null;
    }

    public NakedObjectField[] getFields() {
        return new NakedObjectField[0];
    }

    public String getFullName() {
        return null;
    }

    public Action getObjectAction(Type type, String name) {
        return null;
    }

    public Action getObjectAction(Type type, String name, NakedObjectSpecification[] parameters) {
        return null;
    }

    public Action[] getObjectActions(Type type) {
        return new Action[0];
    }

    public String getPluralName() {
        return name;
    }

    public String getShortName() {
        return name;
    }

    public String getSingularName() {
        return name;
    }

    public ObjectTitle getTitle() {
        return null;
    }

    public NakedObjectField[] getVisibleFields(NakedObject object) {
        return getFields();
    }

    public boolean hasSubclasses() {
        return false;
    }

    public NakedObjectSpecification[] interfaces() {
        return new NakedObjectSpecification[0];
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
        return true;
    }

    public boolean isOfType(NakedObjectSpecification cls) {
        return cls == this;
    }

    public boolean isValue() {
        return false;
    }

    public void markDirty(NakedObject object) {}

    public Persistable persistable() {
        return Persistable.TRANSIENT;
    }

    public NakedObjectSpecification[] subclasses() {
        return new NakedObjectSpecification[0];
    }

    public NakedObjectSpecification superclass() {
        return null;
    }

    public String unresolvedTitle(NakedObject object) {
        return "";
    }

    public void deleted(NakedObject object) {}
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