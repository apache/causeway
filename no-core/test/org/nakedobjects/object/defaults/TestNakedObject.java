package org.nakedobjects.object.defaults;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.TextEntryParseException;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.ActionParameterSet;
import org.nakedobjects.object.security.Session;

public class TestNakedObject implements NakedObject {

    public TestNakedObject() {
        super();
    }

    public void created() {}

    public void deleted() {}

    public String getIconName() {
        return null;
    }

    public Object getObject() {
        return null;
    }

    public Oid getOid() {
        return null;
    }

    public boolean isResolved() {
        return false;
    }

    public boolean isPersistent() {
        return false;
    }

    public void setOid(Oid oid) {}

    public void setResolved() {}

    public NakedObjectContext getContext() {
        return null;
    }

    public void setContext(NakedObjectContext context) {}

    public boolean isEmpty(NakedObjectField field) {
        return false;
    }

    public void clear(OneToOneAssociation specification) {}

    public NakedObject getField(NakedObjectField field) {
        return null;
    }

    public void setAssociation(NakedObjectAssociation field, NakedObject associatedObject) {}

    public void setValue(OneToOneAssociation field, Object object) {}

    public String getLabel(Session session, NakedObjectField field) {
        return null;
    }

    public String getLabel(Session session, Action action) {
        return null;
    }

    public void parseTextEntry(OneToOneAssociation specification, String text) throws TextEntryParseException, InvalidEntryException {}

    public void copyObject(Naked object) {}

    public NakedObjectSpecification getSpecification() {
        return null;
    }

    public boolean isSameAs(Naked object) {
        return false;
    }

    public String titleString() {
        return null;
    }

    public void clear(NakedObjectAssociation specification, NakedObject ref) {}

    public boolean canAccess(Session session, NakedObjectField specification) {
        return false;
    }

    public boolean canAccess(Session session, Action action) {
        return false;
    }

    public boolean canUse(Session session, NakedObjectField field) {
        return false;
    }

    public NakedObject execute(Action action, Naked[] parameters) {
        return null;
    }

    public Hint getHint(Session session, Action action, Naked[] parameters) {
        return null;
    }

    public Hint getHint(Session session, NakedObjectField field, NakedObject value) {
        return null;
    }

    public boolean isParsable() {
        return false;
    }

    public void initAssociation(NakedObjectAssociation field, NakedObject associatedObject) {}

    public void initValue(OneToOneAssociation field, Object object) {}

    public void initOneToManyAssociation(OneToManyAssociation association, NakedObject[] instances) {}

    public void markDirty() {}

    public void clearViewDirty() {}

    public boolean isViewDirty() {
        return false;
    }

    public ActionParameterSet getParameters(Session session, Action action, NakedObjectSpecification[] parameterTypes) {
        return null;
    }

    public boolean isPersistDirty() {
        return false;
    }

    public void clearPersistDirty() {}

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