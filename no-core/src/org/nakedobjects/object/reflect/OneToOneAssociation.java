package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.utility.ToString;


public class OneToOneAssociation extends NakedObjectAssociation {
    private final OneToOnePeer reflectiveAdapter;

    public OneToOneAssociation(String className, String fieldName, NakedObjectSpecification type, OneToOnePeer association) {
        super(fieldName, type, new MemberIdentifier(className, fieldName));
        this.reflectiveAdapter = association;
    }

    protected void clearAssociation(NakedObject inObject, NakedObject associate) {
        if (associate == null) {
            throw new NullPointerException("Must specify the item to remove/dissociate");
        }
        reflectiveAdapter.clearAssociation(getIdentifier(), inObject, associate);
    }

    protected void clearValue(NakedObject inObject) {
        NakedValue associate = (NakedValue) get(inObject);
        associate.clear();
    }

    protected Naked get(NakedObject fromObject) {
        return reflectiveAdapter.getAssociation(getIdentifier(), fromObject);
    }

    public Object getExtension(Class cls) {
        return reflectiveAdapter.getExtension(cls);
    }

    public Class[] getExtensions() {
        return reflectiveAdapter.getExtensions();
    }

    protected Hint getHint(NakedObject object, Naked value) {
        if (hasHint()) {
            return reflectiveAdapter.getHint(getIdentifier(), object, value);
        } else {
            return new DefaultHint();
        }
    }

    protected String getLabel(NakedObject object) {
        Hint hint = getHint(object, get(object));
        return getLabel(hint);
    }

    public boolean hasHint() {
        return reflectiveAdapter.hasHint();
    }

    protected void initAssociation(NakedObject inObject, NakedObject associate) {
        if (readWrite()) {
            reflectiveAdapter.initAssociation(getIdentifier(), inObject, associate);
        }
    }

    private boolean readWrite() {
        return !reflectiveAdapter.isDerived();
    }

    protected void initValue(NakedObject inObject, Object associate) {
        if (readWrite()) {
            reflectiveAdapter.initValue(getIdentifier(), inObject, associate);
        }
    }

    public boolean isDerived() {
        return reflectiveAdapter.isDerived();
    }

    public boolean isMandatory() {
        return reflectiveAdapter.isMandatory();
    }

    public boolean isEmpty(NakedObject inObject) {
        return reflectiveAdapter.isEmpty(getIdentifier(), inObject);
    }

    protected void setAssociation(NakedObject inObject, NakedObject associate) {
        if (readWrite()) {
            reflectiveAdapter.setAssociation(getIdentifier(), inObject, associate);
        }
    }

    protected void setValue(NakedObject inObject, Object value) {
        if (readWrite()) {
            reflectiveAdapter.setValue(getIdentifier(), inObject, value);
        }
    }

    public String toString() {
        ToString str = new ToString(this);
        str.append("type", isValue() ? "VALUE" : "OBJECT");
        str.append(",");
        str.append(super.toString());
        str.append("derived", isDerived());
        str.append("type", getSpecification().getShortName());
        return str.toString();
    }

    public boolean isObject() {
        return getSpecification().isObject();
    }

    public boolean isValue() {
        return getSpecification().isValue();
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