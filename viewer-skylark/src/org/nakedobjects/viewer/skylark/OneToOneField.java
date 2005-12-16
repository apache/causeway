package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.Aggregated;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.OneToOneAssociation;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.viewer.skylark.basic.ClearOneToOneAssociationOption;


public class OneToOneField extends ObjectContent implements FieldContent {
    private static final UserAction CLEAR_ASSOCIATION = new ClearOneToOneAssociationOption();
    private final ObjectField field;
    private final NakedObject object;

    public OneToOneField(NakedObject parent, NakedObject object, OneToOneAssociation association) {
        field = new ObjectField(parent, association);
        this.object = object;
    }

    public Consent canClear() {
        NakedObject parentObject = getParent();
        OneToOneAssociation association = getOneToOneAssociation();
        NakedObject associatedObject = getObject();
        Consent edit = parentObject.isValid(association, associatedObject);
        if (edit.isAllowed()) {
            String status = "Clear the association to this object from '" + parentObject.titleString() + "'";
            return new Allow(status);
        } else {
            return new Veto(edit.getReason());
        }
    }

    public Consent canSet(NakedObject object) {
        if (object.getObject() instanceof NakedClass) {
            return new Allow();
        } else {
            NakedObjectSpecification targetType = getOneToOneAssociation().getSpecification();
            NakedObjectSpecification sourceType = object.getSpecification();
            if (!sourceType.isOfType(targetType)) {
                return new Veto("Can only drop objects of type " + targetType.getSingularName());
            }

            if (getParent().getOid() != null && object.getOid() == null) {
                return new Veto("Can't drop a non-persistent into this persistent object");
            }

            if (object instanceof Aggregated) {
                Aggregated aggregated = ((Aggregated) object);
                if (aggregated.isAggregated() && aggregated.parent() != getParent()) {
                    return new Veto("Object is already associated with another object: " + aggregated.parent());
                }
            }

            Consent perm = getParent().isValid(getOneToOneAssociation(), object);
            return perm;
        }

    }

    public void clear() {
        getParent().clearAssociation(getOneToOneAssociation(), object);
        //        getOneToOneAssociation().clearAssociation(getParent(), object);
    }

    public void debugDetails(DebugString debug) {
        field.debugDetails(debug);
        debug.appendln(4, "object", object);
    }

    public String getFieldName() {
        return field.getName();
    }

    public NakedObjectField getField() {
        return field.getFieldReflector();
    }
    
    public Consent isEditable() {
        return getField().isUsable(getParent());
    }

    /*public String getIconName() {
        return object.getIconName();
    }*/

/*    public Image getIconPicture(int iconHeight) {
        NakedObjectSpecification specification = object.getSpecification();
        return ImageFactory.getInstance().loadIcon(specification, "", iconHeight);
    }
*/
    public Naked getNaked() {
        return object;
    }

    public NakedObject getObject() {
        return object;
    }

    private OneToOneAssociation getOneToOneAssociation() {
        return (OneToOneAssociation) getField();
    }

    private NakedObject getParent() {
        return field.getParent();
    }

    public NakedObjectSpecification getSpecification() {
        return getOneToOneAssociation().getSpecification();
    }
    
    public boolean isDerived() {
        return getOneToOneAssociation().isDerived();
    }

    public boolean isLookup() {
        return getOneToOneAssociation().getSpecification().isLookup();
    }
    
    public boolean isMandatory() {
        return getOneToOneAssociation().isMandatory();
    }
    
    public boolean isPersistable() {
        return getObject() != null && super.isPersistable();
    }

    public boolean isObject() {
        return true;
    }

    public boolean isTransient() {
        return object != null && object.getResolveState().isTransient();
    }

    public void menuOptions(MenuOptionSet options) {
        /*
        if (getObject() == null) {
            ClassOption.menuOptions(getOneToOneAssociation().getSpecification(), options);
        } else {
            ObjectOption.menuOptions(object, options);
            options.add(MenuOptionSet.OBJECT, REMOVE_ASSOCIATION);
        }
        */
        super.menuOptions(options);
        if (getObject() != null) {
            options.add(MenuOptionSet.OBJECT, CLEAR_ASSOCIATION);
        }
    }

    public void setObject(NakedObject object) {
        getParent().setAssociation(getOneToOneAssociation(), object);
    }

    public String title() {
        return object == null ? "" : object.titleString();
    }

    public String toString() {
        return getObject() + "/" + getField();
    }

    public String windowTitle() {
        return title();
    }
    
    
    public String getName() {
        return getOneToOneAssociation().getName();
    }

    public String getDescription() {
        return getOneToOneAssociation().getDescription();
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