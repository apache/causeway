package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.OneToManyAssociation;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.viewer.skylark.basic.ClearOneToManyAssociationOption;

import org.apache.log4j.Logger;


public class OneToManyFieldElement extends ObjectContent implements FieldContent {
    private static final Logger LOG = Logger.getLogger(OneToManyFieldElement.class);
    private static final UserAction REMOVE_ASSOCIATION = new ClearOneToManyAssociationOption();
    private final NakedObject element;
    private final ObjectField field;

    public OneToManyFieldElement(NakedObject parent, NakedObject element, OneToManyAssociation association) {
        field = new ObjectField(parent, association);
        this.element = element;
    }

    public Consent canClear() {
        NakedObject parentObject = getParent();
        OneToManyAssociation association = getOneToManyAssociation();
        NakedObject associatedObject = getObject();
        Consent edit = association.validToRemove(parentObject, associatedObject);
        if (edit.isAllowed()) {
            String status = "Clear the association to this object from '" + parentObject.titleString() + "'";
            return new Allow(status);
        } else {
            return new Veto(edit.getReason());
        }
    }

    public Consent canSet(NakedObject dragSource) {
        return Veto.DEFAULT;
    }

    public void clear() {
        NakedObject parentObject = getParent();
        OneToManyAssociation association = getOneToManyAssociation();
        LOG.debug("remove " + element + " from " + parentObject);
        association.removeElement(parentObject, element);
    }

    public void debugDetails(DebugString debug) {
        field.debugDetails(debug);
        debug.appendln(4, "element", element);
    }

    public String getFieldName() {
        return field.getName();
    }

    public NakedObjectField getField() {
        return field.getFieldReflector();
    }

    public Naked getNaked() {
        return element;
    }

    public NakedObject getObject() {
        return element;
    }

    private OneToManyAssociation getOneToManyAssociation() {
        return (OneToManyAssociation) field.getFieldReflector();
    }

    private NakedObject getParent() {
        return field.getParent();
    }

    public NakedObjectSpecification getSpecification() {
        return field.getSpecification();
    }

    public boolean isMandatory() {
        return false;
    }

    public boolean isObject() {
        return true;
    }
    
    public boolean isTransient() {
        return false;
    }

    public void menuOptions(MenuOptionSet options) {
        //ObjectOption.menuOptions(element, options);
        super.menuOptions(options);
        options.add(MenuOptionSet.OBJECT, REMOVE_ASSOCIATION);
    }

    public void setObject(NakedObject object) {
    /*
     * NakedObject parentObject = getParent(); OneToManyAssociationSpecification
     * association = getOneToManyAssociation(); NakedObject associatedObject =
     * getObject(); LOG.debug("remove " + associatedObject + " from " +
     * parentObject); association.clearAssociation(parentObject,
     * associatedObject);
     */

    }

    public String title() {
        return element.titleString();
    }

    public String toString() {
        return getObject() + "/" + field.getFieldReflector();
    }
    
    public String windowTitle() {
        return field.getName();
    }
    
    public String getName() {
        return getOneToManyAssociation().getName();
    }

    public String getDescription() {
        return getOneToManyAssociation().getDescription();
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