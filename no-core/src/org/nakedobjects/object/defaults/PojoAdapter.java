package org.nakedobjects.object.defaults;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.ActionParameterSet;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectMember;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.OneToManyAssociation;
import org.nakedobjects.object.OneToOneAssociation;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.utility.ToString;

import org.apache.log4j.Logger;


public class PojoAdapter extends AbstractNakedReference implements NakedObject {
    private final static Logger LOG = Logger.getLogger(PojoAdapter.class);
    private Object pojo;

    public PojoAdapter(Object pojo) {
        this.pojo = pojo;
    }

    public void clearAssociation(NakedObjectField field, NakedObject associate) {
        LOG.debug("clearAssociation " + field.getId() + "/" + associate + " in " + this);
        if (field instanceof OneToOneAssociation) {
            ((OneToOneAssociation) field).clearAssociation(this, associate);
        } else {
            ((OneToManyAssociation) field).removeElement(this, associate);            
        }
    }

    public void clearCollection(OneToManyAssociation field) {
        LOG.debug("clearCollection " + field.getId() + " in " + this);
        field.clearCollection(this);
    }

    public void clearValue(OneToOneAssociation field) {
        LOG.debug("clearValue " + field.getId() + " in " + this);
        field.clearValue(this);
    }

    /**
     * Asks the reflector to tell the pojo that this object has been deleted.
     */
    public void destroyed() {
        LOG.debug("deleted notification for " + this);
        getSpecification().deleted(this);
    }

    public boolean equals(Object other) {
        return super.equals(other);
        /*
         * if (other == this) { return true; }
         * 
         * if (other instanceof PojoAdapter) { // we don't delegate to equals(PojoAdapter) because we // don't
         * want to do the identity test again. PojoAdapter otherPojoAdapter = (PojoAdapter) other; return
         * otherPojoAdapter.pojo == pojo; // otherPojoAdapter.pojo.equals(pojo); } return false;
         */}

    /**
     * Overloaded to allow compiler to link directly if we know the compile-time type. (possible performance
     * improvement - called 166,000 times in normal ref data fixture. / public boolean equals(PojoAdapter
     * otherPojoAdapter) { if (otherPojoAdapter == this) { return true; } return otherPojoAdapter.pojo ==
     * pojo; // otherPojoAdapter.pojo.equals(pojo); }
     */

    public Naked execute(Action action, Naked[] parameters) {
        LOG.debug("execute " + action.getId() + " in " + this);
        Naked result = action.execute(this, parameters);
        return result;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        LOG.debug("finalizing pojo: " + pojo);
    }

    public NakedObject getAssociation(OneToOneAssociation field) {
        return (NakedObject) field.get(this);
    }

    public Naked getField(NakedObjectField field) {
        return field.get(this);
    }

    public NakedObjectField[] getFields() {
        return getSpecification().getFields();
    }

    public Consent isValid(OneToOneAssociation field, NakedValue value) {
        return field.isValueValid(this, value);
    }

    public Consent isValid(OneToOneAssociation field, NakedObject nakedObject) {
        return field.isAssociationValid(this, nakedObject);
    }

    public Consent isValid(Action action, Naked[] parameters) {
        return action.hasValidParameters(this, parameters);
    }

    public Consent isVisible(Action action) {
        return action.isVisible(this);
    }

    public Consent isVisible(NakedObjectField field) {
        return field.isVisible(this);
    }

    public String getDescription(NakedObjectMember member) {
        return member.getDescription();
    }

    public Object getObject() {
        return pojo;
    }

    public ActionParameterSet getParameters(Action action) {
        return action.getParameterSet(this);
    }

    public NakedValue getValue(OneToOneAssociation field) {
        return (NakedValue) field.get(this);
    }

    public NakedObjectField[] getVisibleFields() {
        return getSpecification().getVisibleFields(this);
    }

    public void initAssociation(NakedObjectField field, NakedObject associatedObject) {
        LOG.debug("initAssociation " + field.getId() + "/" + associatedObject + " in " + this);
        if (field instanceof OneToOneAssociation) {
            ((OneToOneAssociation) field).initAssociation(this, associatedObject);
        } else {
            ((OneToManyAssociation) field).initElement(this, associatedObject);
        }
    }

    public void initAssociation(OneToManyAssociation field, NakedObject[] instances) {
        LOG.debug("initAssociation " + field.getId() + " with " + instances.length + "instances in " + this);
        field.initCollection(this, instances);
    }

    public void initValue(OneToOneAssociation field, Object object) {
        LOG.debug("initValue " + field.getId() + " with " + object + " in " + this);
        field.initValue(this, object);
    }

    public boolean isEmpty(NakedObjectField field) {
        return field.isEmpty(this);
    }

    public void setAssociation(NakedObjectField field, NakedObject associatedObject) {
        LOG.debug("setAssociation " + field.getId() + " of " + this + " with " + associatedObject);
        if (field instanceof OneToOneAssociation) {
            ((OneToOneAssociation) field).setAssociation(this, associatedObject);
        } else {
            ((OneToManyAssociation) field).addElement(this, associatedObject);
        }
    }

    public void setValue(OneToOneAssociation field, Object object) {
        LOG.debug("setValue " + field.getId() + " with " + object + " in " + this);
        field.setValue(this, object);
    }

    /**
     * Returns the title from the underlying business object. If the object has not yet been resolved the
     * specification will be asked for a unresolved title, which could of been persisted by the persistence
     * mechanism. If either of the above provides null as the title then this method will return a title
     * relating to the name of the object type, e.g. "A Customer", "A Product".
     */
    public String titleString() {
        NakedObjectSpecification specification = getSpecification();
        String title = specification.getTitle(this);
        if (title == null) {
            ResolveState resolveState = getResolveState();
            if (resolveState.isGhost()) {
                LOG.info("attempting to use unresolved object; resolving it immediately: " + this);
                NakedObjects.getObjectPersistor().resolveImmediately(this);
            }
        }
        if (title == null) {
            title = getDefaultTitle();
        }
        return title;
    }

    public synchronized String toString() {
        ToString str = new ToString(this);
        toString(str);

        ResolveState resolveState = getResolveState();
        if (resolveState.isTransient() || resolveState.isResolved()) {
            // don't do title of unresolved objects as this may force the resolving of the object.
            str.append("title", titleString());
        }
        str.appendAsHex("pojo-hash", pojo.hashCode());
        return str.toString();
    }

    public void recreatedAs(Oid oid) {
        changeState(ResolveState.GHOST);
        setOid(oid);
    }

    public Consent canAdd(OneToManyAssociation field, NakedObject element) {
        return field.validToAdd(this, element);
    }
    
    public Consent isUsable(Action action) {
        return action.isUsable(this);
    }
    
    public Consent isUsable(NakedObjectField field) {
        return field.isUsable(this);
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
