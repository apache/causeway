package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.Aggregated;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.OneToManyAssociation;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.utility.NakedObjectRuntimeException;
import org.nakedobjects.viewer.skylark.basic.OptionFactory;
import org.nakedobjects.viewer.skylark.util.ImageFactory;


public class OneToManyField extends CollectionContent implements FieldContent {
    private final NakedCollection collection;
    private final ObjectField field;

    public Consent canDrop(Content sourceContent) {
        if(sourceContent.getNaked() instanceof NakedObject) {
            NakedObject object = (NakedObject) sourceContent.getNaked();
            NakedObject parent = field.getParent();
    
            InternalCollection collection = (InternalCollection) getNaked();
            if(!object.getSpecification().isOfType(collection.getElementSpecification())) {
                return new Veto("Only objects of type " + collection.getElementSpecification().getSingularName() + " are allowed in this collection");
            }
            if(parent.getOid() != null && object.getOid() == null) {
                return new Veto("Can't set field in persistent object with reference to non-persistent object");
            }
            if(object instanceof Aggregated) {
                Aggregated aggregated = ((Aggregated) object);
                if(aggregated.isAggregated() && aggregated.parent() != parent) {
                    return new Veto("Object is already associated with another object: " + aggregated.parent());
                }
            }
            return getOneToManyAssociation().validToAdd(parent, object);
//            Hint about = getOneToManyAssociation().getHint(parent, object, true);
//            return about.canUse();
        } else {
            return Veto.DEFAULT;
        }
    }
    
    public Naked drop(Content sourceContent) {
        NakedObject object = (NakedObject) sourceContent.getNaked();
        NakedObject parent = field.getParent();
        Consent perm = canDrop(sourceContent);
        if (perm.isAllowed()) {
	        parent.setAssociation(getOneToManyAssociation(), object);
        }
        return null;
    }

    public OneToManyField(NakedObject parent, InternalCollection object, OneToManyAssociation association) {
        field = new ObjectField(parent, association);
        this.collection = (NakedCollection) object;
    }

    public NakedObject[] elements() {
        final NakedObject[] elements;
        final int size = getCollection().size();
        elements = new NakedObject[size];
        for (int i = 0; i < size; i++) {
            elements[i] = getCollection().elementAt(i);
        }
        return elements;
    }

    public Consent canClear() {
        return Veto.DEFAULT;
    }

    public Consent canSet(NakedObject dragSource) {
        return Veto.DEFAULT;
    }

    public void clear() {
        throw new NakedObjectRuntimeException("Invalid call");
    }

    public void debugDetails(DebugString debug) {
        field.debugDetails(debug);
        debug.appendln(4, "collection",  collection);
        super.debugDetails(debug); 
    }

    public NakedCollection getCollection() {
        return collection;
    }

    public String getFieldName() {
        return field.getName();
    }

    public NakedObjectField getField() {
        return field.getFieldReflector();
    }

    public String getIconName() {
        return null;
    // return "internal-collection";    
    }

    public Naked getNaked() {
        return collection;
    }

    public OneToManyAssociation getOneToManyAssociation() {
        return (OneToManyAssociation) field.getFieldReflector();
    }

    public NakedObjectSpecification getSpecification() {
        return field.getSpecification();
    }

    public boolean isCollection() {
        return true;
    }

    public boolean isDerived() {
        return getOneToManyAssociation().isDerived();
    }
    
    public boolean isTransient() {
        return false;
    }

    public boolean isMandatory() {
        return getOneToManyAssociation().isMandatory();
    }
    
    public void setObject(NakedObject object) {
        throw new NakedObjectRuntimeException("Invalid call");
    }

    public final String title() {
        return field.getName();
    }

    public String toString() {
        return collection + "/" + field.getFieldReflector();
    }
    
    public String windowTitle() {
        return title() + " for " + field.getParent().titleString();
    }


    public void contentMenuOptions(UserActionSet options) {
        super.contentMenuOptions(options);
        OptionFactory.addClassMenuOptions(getOneToManyAssociation().getSpecification(), options);
    }
    
    public Image getIconPicture(int iconHeight) {
        NakedObjectSpecification specification = getOneToManyAssociation().getSpecification();
        return ImageFactory.getInstance().loadObjectIcon(specification, "", iconHeight);
    }

    public String getId() {
        return getOneToManyAssociation().getId();
    }
    
    public String getDescription() {
        return getOneToManyAssociation().getId();
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