package test.org.nakedobjects.object.reflect;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.ActionParameterSet;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectAssociation;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectMember;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.OneToManyAssociation;
import org.nakedobjects.object.OneToOneAssociation;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.Version;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.utility.ToString;

import java.util.Hashtable;

import junit.framework.Assert;


public class DummyNakedObject implements NakedObject {
    private Hashtable fieldContents = new Hashtable();
    private NakedObjectField[] fields = new NakedObjectField[0];
    private String label;
    private Object object;
    private Oid oid;
    private NakedObjectSpecification spec;
    private ResolveState state;
    private String titleString;
    private Version version;
    private static int next;
    private final int id = next++;
    

    public DummyNakedObject() {
        super();
    }
    
    public DummyNakedObject(String title) {
        this.titleString = title;
    }
    
    public void assertFieldContains(String name, Object value) {
        Object content = fieldContents.get(name);
        Assert.assertNotNull("No field: " + name, content);
        Assert.assertTrue("Expected to find " + value + " in field " + name + ", but found "+ content, content.equals(value));
    }

    public void checkLock(Version version) {}

    public void clearAssociation(NakedObjectAssociation specification, NakedObject ref) {}

    public void clearCollection(OneToManyAssociation association) {}

    public void clearValue(OneToOneAssociation specification) {}

    public void debugClearResolved() {}

    public void destroyed() {}

    public Naked execute(Action action, Naked[] parameters) {
        return null;
    }

    public NakedObject getAssociation(OneToOneAssociation field) {
        return (NakedObject) getField(field);
    }

    public Naked getField(NakedObjectField field) {
        return (Naked) fieldContents.get(field.getName());
    }

    public NakedObjectField[] getFields() {
        return fields;
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

    public ResolveState getResolveState() {
        return state;
    }

    public NakedObjectSpecification getSpecification() {
        return spec;
    }

    public NakedValue getValue(OneToOneAssociation field) {
        return null;
    }

    public Version getVersion() {
        return version;
    }

    public NakedObjectField[] getVisibleFields() {
        return getFields();
    }

    public void initAssociation(NakedObjectAssociation field, NakedObject associatedObject) {
        init(field, associatedObject);
    }

    public void initAssociation(OneToManyAssociation association, NakedObject[] instances) {
        init(association, instances);
    }

    
    public void initValue(OneToOneAssociation field, Object object) {
        init(field, object);
    }

    private void init(NakedObjectMember field, Object object) {
        String name = field.getName();
        fieldContents.put(name, object);
    }

    public boolean isEmpty(NakedObjectField field) {
        return false;
    }

    public Persistable persistable() {
        return null;
    }

    public void setAssociation(NakedObjectAssociation field, NakedObject associatedObject) {}

    public void setOptimisticLock(Version version) {
        this.version = version;
    }

    public void setupFields(NakedObjectField[] fields) {
        this.fields = fields;
    }

    public void setupFieldValue(String name, Naked field) {
        this.fieldContents.put(name, field);
    }

    public void setupLabel(String label) {
        this.label = label;
    }

    public void setupObject(Object object) {
        this.object = object;
    }

    public void setupOid(Oid oid) {
        this.oid = oid;
    }

    public void setupResolveState(ResolveState state) {
        this.state = state;
    }

    public void setupSpecification(NakedObjectSpecification spec) {
        this.spec = spec;
    }

    public void setupTitleString(String titleString) {
        this.titleString = titleString;
    }

    public void setupVersion(Version version) {
        this.version = version;
    }

    public void setValue(OneToOneAssociation field, Object object) {}

    public String titleString() {
        return titleString;
    }
    
    public String toString() {
        ToString str = new ToString(this, id);
        str.append("title", titleString);
        str.append("pojo", object);
        return str.toString();
    }

    public Consent isValid(OneToOneAssociation field, NakedValue nakedValue) {
        return null;
    }

    public Consent isValid(OneToOneAssociation field, NakedObject nakedObject) {
        return null;
    }

    public Consent canAdd(OneToManyAssociation field, NakedObject nakedObject) {
        return null;
    }

    public Consent isValid(Action action, Naked[] parameters) {
        return null;
    }

    public Consent isVisible(NakedObjectField field) {
        return null;
    }

    public Consent isVisible(Action action) {
        return null;
    }

    public void persistedAs(Oid oid) {}

    public void changeState(ResolveState newState) {}
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