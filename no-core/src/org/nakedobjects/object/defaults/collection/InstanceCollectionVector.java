package org.nakedobjects.object.defaults.collection;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.TextEntryParseException;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.object.defaults.InternalNakedObject;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.ActionParameterSet;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.utility.UnexpectedCallException;

import java.util.Enumeration;
import java.util.Vector;


public class InstanceCollectionVector implements TypedNakedCollection, InternalNakedObject {

    private String name;
    private Vector elements;
    private long version;
    public long getVersion() {
        return version;
    }
    public void setVersion(long version) {
        this.version = version;
    }
    private NakedObjectSpecification specification;
    private NakedObjectSpecification elementSpecification;

    public InstanceCollectionVector(NakedObjectSpecification elementSpecification, NakedObject[] instances) {
        this.elementSpecification = elementSpecification;
        name = elementSpecification.getPluralName();
        
        int size = instances.length;
        elements = new Vector(size);
        for (int i = 0; i < size; i++) {
            elements.addElement(instances[i]);
        }
    }

    /**
     * Returns a veto. The user should not be able to add instannces to the set
     * of instances
     */
    public Consent canAdd(NakedObject object) {
        return Veto.DEFAULT;
    }

    /**
     * Returns a veto. The user should not be able to remove instannces to the
     * set of instances
     */
    public Consent canRemove(NakedObject object) {
        return Veto.DEFAULT;
    }

    public NakedObject elementAt(int i) {
        if(i < 0 || i >= size()) {
            throw new IllegalArgumentException("No such element: " + i);
        }
        return (NakedObject) elements.elementAt(i);
    }

    /**
     * The instances collections are always shown as persistent as they are
     * based on the ObjectStore, which is used to persist objects, although the
     * collection does not exist in its own right on the store.
     */
    public boolean isPersistent() {
        return true;
    }

    public void resolve() {}

    public String titleString() {
//        return getElementSpecification().getPluralName() + "(" + size() + ")";
        return name + "(" + size() + ")";
    }

    public int size() {
        return elements.size();
    }

    public void clear(OneToOneAssociation specification) {}

    public NakedObject getField(NakedObjectField field) {
        return null;
    }

    public void setAssociation(NakedObjectAssociation field, NakedObject associatedObject) {}

    public void initAssociation(NakedObjectAssociation field, NakedObject associatedObject) {}
    
    public void setValue(OneToOneAssociation field, Object object) {}

    public void initValue(OneToOneAssociation field, Object object) {}
    
    public String getLabel(NakedObjectField field) {
        return null;
    }

    public String getLabel(Action action) {
        return null;
    }

    public Enumeration getFieldElements(NakedObjectAssociation oneToManyAssociation) {
        return null;
    }

    public void clear() {}
    
    public void clearAssociation(NakedObjectAssociation specification, NakedObject ref) {}

    public Naked execute(Action action, Naked[] parameters) {
        return null;
    }

    public Hint getHint(Action action, Naked[] parameters) {
        return null;
    }

    public Hint getHint(NakedObjectField field, Naked value) {
        return null;
    }

    public NakedObjectSpecification getElementSpecification() {
        return elementSpecification;
    }

    public boolean contains(NakedObject object) {
        return false;
    }

    public Enumeration elements() {
        return elements.elements();
    }

    public boolean isEmpty() {
        return false;
    }

    public Enumeration oids() {
        return null;
    }

    public void created() {}

    public void deleted() {}

    public String getIconName() {
        return null;
    }

    public Object getObject() {
        return null;
    }

    public Oid getOid() {
        return null;
    }

    public boolean isResolved() {
        return false;
    }

    public void setOid(Oid oid) {}

    public void setResolved() {}

    public boolean isEmpty(NakedObjectField field) {
        return false;
    }

    public void parseTextEntry(OneToOneAssociation specification, String text) throws TextEntryParseException, InvalidEntryException {}

    public void copyObject(Naked object) {}

    public NakedObjectSpecification getSpecification() {
        if(specification == null) {
            specification = NakedObjects.getSpecificationLoader().loadSpecification(this.getClass());
        }
        return specification;
    }
    
    public boolean isParsable() {
        return false;
    }

    public void initOneToManyAssociation(OneToManyAssociation association, NakedObject[] instances) {}

    public void markDirty() {}

    public void clearViewDirty() {}

    public boolean isViewDirty() {
        return false;
    }

    public ActionParameterSet getParameters(Action action, NakedObjectSpecification[] parameterTypes) {
        throw new UnexpectedCallException();
    }

    public boolean isPersistDirty() {
        return false;
    }

    public void clearPersistDirty() {}

    public void sort() {
        Vector sorted = new Vector(elements.size());
        
        outer:
        for(Enumeration e = elements.elements(); e.hasMoreElements();) {
            NakedObject  element = (NakedObject)  e.nextElement();
            String title = element.titleString();
            
            int i = 0;
            for(Enumeration f = sorted.elements(); f.hasMoreElements();) {
                NakedObject  sortedElement = (NakedObject)  f.nextElement();
                String sortedTitle = sortedElement.titleString();
                if(sortedTitle.compareTo(title) > 0) {
                    sorted.insertElementAt(element, i);
                    continue outer;
                }
                i++;
            }
            sorted.addElement(element);
        }
        
        elements = sorted;
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