package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.persistence.Oid;

import java.util.Date;
import java.util.Hashtable;


public class DummyNakedObject implements NakedObject {
    private NakedObjectField[] fields = new NakedObjectField[0];
    private String label;
    private Object object;
    private Oid oid;
    private NakedObjectSpecification spec;
    private Hint hint;
     private long version;
     private boolean resolved;
    private String titleString;
    private Hashtable fieldContents = new Hashtable();

     
    public DummyNakedObject() {
        super();
    }

    public void checkLock(long version) {}

    public void clearAssociation(NakedObjectAssociation specification, NakedObject ref) {}

    public void clearCollection(OneToManyAssociation association) {}

    public void clearPersistDirty() {}

    public void clearValue(OneToOneAssociation specification) {}

    public void clearViewDirty() {}

    public void copyObject(Naked object) {}

    public void created() {}

    public void debugClearResolved() {}

    public void destroyed() {}

    public Naked execute(Action action, Naked[] parameters) {
        return null;
    }

    public NakedObject getAssociation(OneToOneAssociation field) {
        return null;
    }
    
    public NakedObjectField[] getFields() {
        return fields;
    }

    public Hint getHint(Action action, Naked[] parameters) {
        return hint;
    }

    public Hint getHint(NakedObjectField field, Naked value) {
        return hint;
    }

    public String getIconName() {
        return null;
    }

    public String getLabel(Action action) {
        return label;
    }

    public String getLabel(NakedObjectField field) {
        return label;
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
    
    public long getVersion() {
        return version;
    }


    public NakedObjectField[] getVisibleFields() {
        return getFields();
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

    public boolean isPartlyResolved() {
        return false;
    }

    public boolean isPersistDirty() {
        return false;
    }

    public boolean isPersistent() {
        return false;
    }


    public boolean isResolving() {
        return false;
    }

    public boolean isUnresolved() {
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

    public void setOptimisticLock(long version, String user, Date time) {}

    public void setResolved() {
        resolved = true;
    }

    public void setupFields(NakedObjectField[] fields) {
        this.fields = fields;
    }

    public void setupLabel(String label) {
        this.label = label;
    }

    public void setupObject(Object object) {
        this.object = object;
    }

    public void setupSpecification(NakedObjectSpecification spec) {
        this.spec = spec;
    }

    public void setupHint(Hint hint) {
        this.hint = hint;
    }

    public void setValue(OneToOneAssociation field, Object object) {}

    public void setVersion(long version) {
        this.version = version;
    }
  
    public boolean isResolved() {
        return resolved;
    }

    public void setupTitleString(String titleString) {
        this.titleString = titleString;
    }

    public String titleString() {
        return titleString;
    }
  
    public Naked getField(NakedObjectField field) {
        return (Naked) fieldContents.get(field.getName());
    }

    public void setupFieldValue(String name, Naked field) {
        this.fieldContents.put(name, field);
    }

    public boolean isTransient() {
        return false;
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