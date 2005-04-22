package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action.Type;
import org.nakedobjects.object.security.Session;


public class NakedObjectSpecificationProvidingTitle implements NakedObjectSpecification {

    public Naked acquireInstance() {
        return null;
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
        return null;
    }

    public Hint getClassHint() {
        return null;
    }

    public NakedObjectField getField(String name) {
        return null;
    }

    public NakedObjectField[] getFields() {
        return null;
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
        return null;
    }

    public String getPluralName() {
        return null;
    }

    public String getShortName() {
        return null;
    }

    public String getSingularName() {
        return null;
    }

    public ObjectTitle getTitle() {
        return new ObjectTitle() {
            public String title(NakedObject object) {
                return "object title from specification";
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

    public boolean isDirty(NakedObject object) {
        return false;
    }

    public boolean isLookup() {
        return false;
    }

    public boolean isObject() {
        return false;
    }

    public boolean isOfType(NakedObjectSpecification cls) {
        return false;
    }

    public boolean isParsable() {
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