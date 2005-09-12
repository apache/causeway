package org.nakedobjects.object;

import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.Action.Type;


public class DummyNakedObjectSpecification implements NakedObjectSpecification {
    private static int next = 100;
    private Action action;
    public NakedObjectField[] fields = new NakedObjectField[0];
    private final int id = next++;
    private boolean isObject = true;
    private boolean isValue = false;
    private NakedObjectSpecification[] subclasses = new NakedObjectSpecification[0];
    private String name;
    private String title;

    public DummyNakedObjectSpecification(String name) {
        this.name = name == null ? "DummyNakedObjectSpecification#" + id : name;
        title = "";
    }

    public DummyNakedObjectSpecification() {
        this(null);
    }

    public void clearDirty(NakedObject object) {}

    public String debugInterface() {
        return null;
    }

    public void deleted(NakedObject object) {}

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
        return null;
    }

    public NakedObjectField getField(String name) {
        return null;
    }

    public Object getFieldExtension(String name, Class cls) {
        return null;
    }
    
    public Class[] geFieldExtensions(String name) {
        return null;
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public NakedObjectField[] getFields() {
        return fields;
    }

    public String getFullName() {
        return name;
    }

    public Action getObjectAction(Type type, String name) {
        return action;
    }

    public Action getObjectAction(Type type, String name, NakedObjectSpecification[] parameters) {
        return action;
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

    public NakedObjectField[] getVisibleFields(NakedObject object) {
        return null;
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
        return isObject;
    }

    public boolean isOfType(NakedObjectSpecification cls) {
        return cls == this;
    }

    public boolean isParsable() {
        return false;
    }

    public boolean isValue() {
        return isValue;
    }

    public void markDirty(NakedObject object) {}

    public Persistable persistable() {
        return null;
    }

    public void setupAction(Action action) {
        this.action = action;
    }

    public void setupFields(NakedObjectField[] fields) {
        this.fields = fields;
    }

    public void setupIsObject() {
        isObject = true;
        isValue = false;
    }

    public void setupIsValue() {
        isValue = true;
        isObject = false;
    }

    public void setupIsCollection() {
        isValue = false;
        isObject = false;
    }

    public void setupSubclasses(NakedObjectSpecification[] subclasses) {
        this.subclasses = subclasses;
    }

    public void setupTitle(String title) {
        this.title = title;
    }

    public NakedObjectSpecification[] subclasses() {
        return subclasses;
    }

    public NakedObjectSpecification superclass() {
        return null;
    }

    public String toString() {
        return getFullName();
    }

    public String getTitle(NakedObject naked) {
        return title;
    }
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