package org.nakedobjects.object;
import org.nakedobjects.object.control.ClassAbout;
import org.nakedobjects.object.reflect.ActionSpecification;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.reflect.ActionSpecification.Type;
import org.nakedobjects.object.security.Session;

public class DummyNakedObjectSpecification implements NakedObjectSpecification {

    public Naked acquireInstance() {
        return null;
    }

    public ClassAbout getClassAbout() {
        return null;
    }

    public ActionSpecification getClassAction(Type type, String name) {
        return null;
    }

    public ActionSpecification getClassAction(Type type, String name, NakedObjectSpecification[] parameters) {
        return null;
    }

    public ActionSpecification[] getClassActions(Type type) {
        return null;
    }

    public ActionSpecification[] getClassActions(Type type, int noParameters) {
        return null;
    }

    public FieldSpecification getField(String name) {
        return null;
    }

    public FieldSpecification[] getFields() {
        return null;
    }

    public String getFullName() {
        return null;
    }

    public ActionSpecification getObjectAction(Type type, String name) {
        return null;
    }

    public ActionSpecification getObjectAction(Type type, String name, NakedObjectSpecification[] parameters) {
        return null;
    }

    public ActionSpecification[] getObjectActions(Type type) {
        return null;
    }

    public ActionSpecification[] getObjectActions(Type type, int noParameters) {
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

    public FieldSpecification[] getVisibleFields(NakedObject object, Session session) {
        return null;
    }

    public boolean hasSubclasses() {
        return false;
    }

    public boolean isCollection() {
        return false;
    }

    public boolean isOfType(NakedObjectSpecification cls) {
        return false;
    }

    public NakedObjectSpecification superclass() {
        return null;
    }

    public NakedObjectSpecification[] subclasses() {
        return null;
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isPartOf() {
        return false;
    }

    public boolean isValue() {
        return false;
    }

    public boolean isObject() {
        return false;
    }

    public String debugInterface() {
        return null;
    }

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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