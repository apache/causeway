package org.nakedobjects.object.reflect;

import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.persistence.Oid;


public class DummyNakedObject implements NakedObject {

    private DummyNakedObjectSpecification spec;
    private Oid oid;
    private Object object;
    private String title;

    public DummyNakedObject() {
        super();
    }

    public void clearAssociation(NakedObjectAssociation specification, NakedObject ref) {}

    public void clearCollection(OneToManyAssociation association) {}

    public void clearPersistDirty() {}

    public void clearValue(OneToOneAssociation specification) {}

    public void clearViewDirty() {}

    public void copyObject(Naked object) {}

    public void created() {}

    public void deleted() {}

    public Naked execute(Action action, Naked[] parameters) {
        return null;
    }

    public NakedObject getAssociation(OneToOneAssociation field) {
        return null;
    }

    public Naked getField(NakedObjectField field) {
        return null;
    }

    public Hint getHint(Action action, Naked[] parameters) {
        return null;
    }

    public Hint getHint(NakedObjectField field, Naked value) {
        return null;
    }

    public String getIconName() {
        return null;
    }

    public String getLabel(Action action) {
        return null;
    }

    public String getLabel(NakedObjectField field) {
        return null;
    }

    public Object getObject() {
        return object;
    }

    public Oid getOid() {
        return oid;
    }

    public ActionParameterSet getParameters(Action action) {
        return null;
    }

    public NakedObjectSpecification getSpecification() {
        return spec;
    }

    public NakedValue getValue(OneToOneAssociation field) {
        return null;
    }

    public void initAssociation(NakedObjectAssociation field, NakedObject associatedObject) {}

    public void initOneToManyAssociation(OneToManyAssociation association, NakedObject[] instances) {}

    public void initValue(OneToOneAssociation field, Object object) {}

    public boolean isEmpty(NakedObjectField field) {
        return false;
    }

    public boolean isParsable() {
        return false;
    }

    public boolean isPersistDirty() {
        return false;
    }

    public boolean isPersistent() {
        return false;
    }

    public boolean isResolved() {
        return false;
    }

    public boolean isSameAs(Naked object) {
        return false;
    }

    public boolean isViewDirty() {
        return false;
    }

    public void markDirty() {}

    public Persistable persistable() {
        return null;
    }

    public void setAssociation(NakedObjectAssociation field, NakedObject associatedObject) {}

    public void setOid(Oid oid) {
        this.oid = oid;
    }

    public void setResolved() {}

    public void setupSpecification(DummyNakedObjectSpecification spec) {
        this.spec = spec;
    }

    public void setValue(OneToOneAssociation field, Object object) {}

    public String titleString() {
        return title;
    }

    public void setupTitle(String title) {
        this.title = title;
    }

    public void setupObject(Object object) {
        this.object = object;
    }

    public NakedObjectField[] getFields() {
        return new NakedObjectField[0];
    }

    public NakedObjectField[] getVisibleFields() {
        return  new NakedObjectField[0];
    }

    public void debugClearResolved() {}

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