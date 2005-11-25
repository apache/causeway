package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.OneToOneAssociation;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.utility.ToString;


public class OneToOneAssociationImpl extends AbstractNakedObjectField implements OneToOneAssociation {
    private final OneToOnePeer reflectiveAdapter;

    public OneToOneAssociationImpl(String className, String fieldName, NakedObjectSpecification specification, OneToOnePeer association) {
        super(fieldName, specification);
        this.reflectiveAdapter = association;
    }

    public void clearAssociation(NakedObject inObject, NakedObject associate) {
        if (associate == null) {
            throw new NullPointerException("Must specify the item to remove/dissociate");
        }
        reflectiveAdapter.clearAssociation(inObject, associate);
    }

    public void clearValue(NakedObject inObject) {
        NakedValue associate = (NakedValue) get(inObject);
        associate.clear();
    }

    public Naked get(NakedObject fromObject) {
        return reflectiveAdapter.getAssociation(fromObject);
    }

    public Object getExtension(Class cls) {
        return reflectiveAdapter.getExtension(cls);
    }

    public Class[] getExtensions() {
        return reflectiveAdapter.getExtensions();
    }
    
    /**
     * Return the default label for this member. This is based on the name of
     * this member.
     * 
     * @see #getId()
     */
    public String getName() {
        String label = reflectiveAdapter.getName();
        return label == null ? defaultLabel : label;
    }

    public void initAssociation(NakedObject inObject, NakedObject associate) {
        if (readWrite()) {
            reflectiveAdapter.initAssociation(inObject, associate);
        }
    }

    private boolean readWrite() {
        return !reflectiveAdapter.isDerived();
    }

    public void initValue(NakedObject inObject, Object associate) {
        if (readWrite()) {
            reflectiveAdapter.initValue(inObject, associate);
        }
    }

    public boolean isDerived() {
        return reflectiveAdapter.isDerived();
    }

    public boolean isHidden() {
        return reflectiveAdapter.isHidden();
    }

    public boolean isMandatory() {
        return reflectiveAdapter.isMandatory();
    }

    public boolean isEmpty(NakedObject inObject) {
        return reflectiveAdapter.isEmpty(inObject);
    }

    public void setAssociation(NakedObject inObject, NakedObject associate) {
        if (readWrite()) {
            reflectiveAdapter.setAssociation(inObject, associate);
        }
    }

    public void setValue(NakedObject inObject, Object value) {
        if (readWrite()) {
            reflectiveAdapter.setValue(inObject, value);
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
        return reflectiveAdapter.isObject();
    }

    public boolean isValue() {
        return  ! reflectiveAdapter.isObject();
    }

    
    
    
    public Consent isValueValid(NakedObject inObject, NakedValue value) {
        return reflectiveAdapter.isValueValid(inObject, value);
    }

    public Consent isAssociationValid(NakedObject inObject, NakedObject value) {
        return reflectiveAdapter.isAssociationValid(inObject, value);
    }

    public Consent isUsable(NakedObject inObject) {
        return reflectiveAdapter.isUsable(inObject);
    }

    public String getDescription() {
        return reflectiveAdapter.getDescription();
    }

    public Consent isVisible(NakedObject target) {
        return reflectiveAdapter.isVisible(target);
    }

    public boolean isAuthorised() {
        return reflectiveAdapter.isAuthorised(NakedObjects.getCurrentSession());
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