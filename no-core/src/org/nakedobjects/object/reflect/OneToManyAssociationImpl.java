package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.OneToManyAssociation;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.utility.ToString;


public class OneToManyAssociationImpl extends AbstractNakedObjectField implements OneToManyAssociation {
    private final OneToManyPeer reflectiveAdapter;

    public OneToManyAssociationImpl(String className, String methodName, NakedObjectSpecification type, OneToManyPeer association) {
        super(methodName, type);
        this.reflectiveAdapter = association;
    }

    public void removeElement(NakedObject inObject, NakedObject associate) {
        if (associate == null) {
            throw new IllegalArgumentException("element should not be null");
        }
        if (readWrite()) {
            reflectiveAdapter.removeAssociation(inObject, associate);
        }
    }

    public void clearCollection(NakedObject inObject) {
        if (readWrite()) {
            reflectiveAdapter.removeAllAssociations(inObject);
        }
    }

    public Naked get(NakedObject fromObject) {
        return reflectiveAdapter.getAssociations(fromObject);
    }

    public Object getExtension(Class cls) {
        return reflectiveAdapter.getExtension(cls);
    }

    public Class[] getExtensions() {
        return reflectiveAdapter.getExtensions();
    }

    private boolean readWrite() {
        return !reflectiveAdapter.isDerived();
    }

    public void initElement(NakedObject inObject, NakedObject associate) {
        if (readWrite()) {
            reflectiveAdapter.initAssociation(inObject, associate);
        }
    }

    public void initCollection(NakedObject inObject, NakedObject[] instances) {
        if (readWrite()) {
            reflectiveAdapter.initOneToManyAssociation(inObject, instances);
        }
    }

    public boolean isCollection() {
        return true;
    }

    public boolean isDerived() {
        return reflectiveAdapter.isDerived();
    }

    public String getDescription() {
        return "";
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
    
    public boolean isEmpty(NakedObject inObject) {
        return reflectiveAdapter.isEmpty(inObject);
    }

    public boolean isHidden() {
        return reflectiveAdapter.isHidden();
    }

    public boolean isPart() {
        return true;
    }

    public void addElement(NakedObject inObject, NakedObject associate) {
        if (associate == null) {
            throw new IllegalArgumentException("Can't use null to add an item to a collection");
        }
        if (readWrite()) {
            reflectiveAdapter.addAssociation(inObject, associate);
        }
    }

    public String toString() {
        ToString str = new ToString(this);
        str.append(super.toString());
        str.append(",");
        str.append("derived", isDerived());
        str.append("type", getSpecification() == null ? "unknown" : getSpecification().getShortName());
        return str.toString();
    }

    
    
    
    
    public Consent validToRemove(NakedObject container, NakedObject element) {
      return reflectiveAdapter.isRemoveValid(container, element);
    }
    
    public Consent validToAdd(NakedObject container, NakedObject element) {
        return reflectiveAdapter.isAddValid(container, element);
    }

    public Consent isAvailable(NakedObject target) {
        return reflectiveAdapter.isUsable(target);
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
