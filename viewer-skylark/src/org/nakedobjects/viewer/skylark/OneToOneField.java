package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.Aggregated;
import org.nakedobjects.object.Lookup;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.control.defaults.Allow;
import org.nakedobjects.object.control.defaults.Veto;
import org.nakedobjects.object.reflect.OneToOneAssociationSpecification;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.viewer.skylark.basic.ClassOption;
import org.nakedobjects.viewer.skylark.basic.ObjectOption;
import org.nakedobjects.viewer.skylark.basic.RemoveOneToOneAssociationOption;


public class OneToOneField extends ObjectField implements ObjectContent {
    private static final UserAction REMOVE_ASSOCIATION = new RemoveOneToOneAssociationOption();
    private final NakedObject object;

    public OneToOneField(NakedObject parent, NakedObject object, OneToOneAssociationSpecification association) {
        super(parent, association);
        this.object = object;
    }

    public Permission canClear() {
        NakedObject parentObject = getParent();
        OneToOneAssociationSpecification association = getOneToOneAssociation();
        NakedObject associatedObject = getObject();
        About about = association.getAbout(ClientSession.getSession(), parentObject, associatedObject);
        Permission edit = about.canUse();
        if (edit.isAllowed()) {
            String status = "Clear the association to this object from '" + parentObject.titleString() + "'";
            return new Allow(status);
        } else {
            return new Veto(edit.getReason());
        }
    }
    
    public Permission canSet(NakedObject object) {
        if (object instanceof NakedClass) {
            return new Allow();
        } else {
            NakedObjectSpecification targetType = getOneToOneAssociation().getType();
            NakedObjectSpecification sourceType = object.getSpecification();
            if (!sourceType.isOfType(targetType)) {
                return new Veto("Can only drop objects of type " + targetType.getSingularName());
            }

            if (getParent().getOid() != null && object.getOid() == null) {
                return new Veto("Can't drop a non-persistent into this persistent object");
            }

            if(object instanceof Aggregated) {
                Aggregated aggregated = ((Aggregated) object);
                if(aggregated.isAggregated() && aggregated.parent() != getParent()) {
                    return new Veto("Object is already associated with another object: " + aggregated.parent());
                }
            }

            Permission perm = getOneToOneAssociation().getAbout(ClientSession.getSession(), getParent(), object).canUse();
            return perm;
        }

    }
    
    public void clear() {
        getOneToOneAssociation().clearAssociation(getParent(), object);
    }

    public String debugDetails() {
        return super.debugDetails() + "  object:" + object + "\n";
    }

    public NakedObject getObject() {
        return object;
    }

    private OneToOneAssociationSpecification getOneToOneAssociation() {
        return (OneToOneAssociationSpecification) getField();
    }

    public NakedObjectSpecification getType() {
        return getOneToOneAssociation().getType();
    }

    public boolean isLookup() {
        NakedObjectSpecification lookup = NakedObjectSpecificationLoader.getInstance().loadSpecification(Lookup.class);
        return getOneToOneAssociation().getType().isOfType(lookup);
    }

    public void menuOptions(MenuOptionSet options) {
        super.menuOptions(options);
        if(getObject() == null) {
            ClassOption.menuOptions(getOneToOneAssociation().getType(), options);
        } else {
            ObjectOption.menuOptions(object, options);
            options.add(MenuOptionSet.OBJECT, REMOVE_ASSOCIATION);
        }
    }

    public void setObject(NakedObject object) {
        NakedObject associatedObject;
        if (object instanceof NakedClass) {
            associatedObject = ((NakedClass) object).newInstance();
        } else {
            associatedObject = object;
        }

 //       getViewManager().getUndoStack().add(new AssociateCommand(target, associatedObject, field));
        getOneToOneAssociation().setAssociation(getParent(), associatedObject);
    }

    public String toString() {
        return getObject() + "/" + getField();
    }
    /*
    public String getName() {
        return getOneToOneAssociation().getType().getSingularName();
    }
    */
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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