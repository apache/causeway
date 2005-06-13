package org.nakedobjects.object.reflect;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.defaults.AbstractNakedObject;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.utility.ToString;

import org.apache.log4j.Logger;


public class PojoAdapter extends AbstractNakedObject {
    private final static Logger LOG = Logger.getLogger(PojoAdapter.class);
    private Object pojo;
    private long version;

    public long getVersion() {
        return version;
    }
    
    public void setVersion(long version) {
        this.version = version;
    }
    
    protected PojoAdapter(Object pojo) {
        this.pojo = pojo;
        LOG.debug("created " + this + " for " + pojo);
    }

    public void clearAssociation(NakedObjectAssociation specification, NakedObject associate) {
        mustBeResolvedIfPersistent(this);
        specification.clearAssociation(this, associate);
    }

    private void mustBeResolvedIfPersistent(NakedObject object) {
        if (object.isPersistent() && !object.isResolved()) {
            LOG.info("Unresolved object attempting to be used; resolving it immediately: " + object);
            NakedObjects.getObjectManager().resolveImmediately(object);
            //throw new NakedObjectRuntimeException("Object not resolved when used with adapter: " + object);
        }
    }

    public void clearCollection(OneToManyAssociation association) {
        mustBeResolvedIfPersistent(this);
        association.clearCollection(this);
    }

    public void clearValue(OneToOneAssociation association) {
        mustBeResolvedIfPersistent(this);
        association.clearValue(this);
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (other instanceof PojoAdapter) {
            // we don't delegate to equals(PojoAdapter) because we
            // don't want to do the identity test again.
            PojoAdapter otherPojoAdapter = (PojoAdapter) other;
            return otherPojoAdapter.pojo == pojo; // otherPojoAdapter.pojo.equals(pojo);
        }
        return false;
    }

    /**
     * Overloaded to allow compiler to link directly if we know the compile-time
     * type. (possible performance improvement - called 166,000 times in normal
     * ref data fixture.
     */
    public boolean equals(PojoAdapter otherPojoAdapter) {
        if (otherPojoAdapter == this) {
            return true;
        }
        return otherPojoAdapter.pojo == pojo; // otherPojoAdapter.pojo.equals(pojo);
    }

    public Naked execute(Action action, Naked[] parameters) {
        mustBeResolvedIfPersistent(this);
        for (int i = 0; parameters != null && i < parameters.length; i++) {
            if (parameters[i] instanceof NakedObject) {
                mustBeResolvedIfPersistent((NakedObject) parameters[i]);
            }
        }
        Naked result = action.execute(this, parameters);
        return result;
    }

    public NakedObject getAssociation(OneToOneAssociation field) {
        mustBeResolvedIfPersistent(this);
        return (NakedObject) field.get(this);
    }

    public Naked getField(NakedObjectField field) {
        mustBeResolvedIfPersistent(this);
        return field.get(this);
    }

    public NakedObjectField[] getFields() {
        return getSpecification().getFields();
    }

    public NakedObjectField[] getVisibleFields() {
        return getSpecification().getVisibleFields(this);
    }

    public Hint getHint(Action action, Naked[] parameterValues) {
        mustBeResolvedIfPersistent(this);
        return action.getHint(this, parameterValues);
    }

    public Hint getHint(NakedObjectField field, Naked value) {
        mustBeResolvedIfPersistent(this);
        if (field instanceof OneToOneAssociation) {
            return ((OneToOneAssociation) field).getHint(this, value);
        } else if (field instanceof OneToManyAssociation) {
            return ((OneToManyAssociation) field).getHint(this);
        } else {
            throw new NakedObjectRuntimeException();
        }
    }

    public String getLabel(Action action) {
        return action.getLabel(this);
    }

    public String getLabel(NakedObjectField field) {
        return field.getLabel(this);
    }

    public Object getObject() {
        return pojo;
    }

    public ActionParameterSet getParameters(Action action) {
        return action.getParameters(this);
    }

    public NakedValue getValue(OneToOneAssociation field) {
        mustBeResolvedIfPersistent(this);
        return (NakedValue) field.get(this);
    }

    public void initAssociation(NakedObjectAssociation field, NakedObject associatedObject) {
        mustBeResolvedIfPersistent(this);
        field.initAssociation(this, associatedObject);
    }

    public void initOneToManyAssociation(OneToManyAssociation field, NakedObject[] instances) {
        mustBeResolvedIfPersistent(this);
        field.initOneToManyAssociation(this, instances);
    }

    public void initValue(OneToOneAssociation field, Object object) {
        mustBeResolvedIfPersistent(this);
        field.initValue(this, object);
    }

    public boolean isEmpty(NakedObjectField field) {
        mustBeResolvedIfPersistent(this);
        return field.isEmpty(this);
    }

    public Persistable persistable() {
        return getSpecification().persistable();
    }

    public boolean isParsable() {
        return getSpecification().isParsable();
    }

    public void setAssociation(NakedObjectAssociation field, NakedObject associatedObject) {
        mustBeResolvedIfPersistent(this);
        field.setAssociation(this, associatedObject);
    }

    public void setValue(OneToOneAssociation field, Object object) {
        mustBeResolvedIfPersistent(this);
        field.setValue(this, object);
    }

    /**
     * Introduced during performance profiling; PojoAdapter#titleString often
     * called.
     */
    private String defaultTitle;
 
    /**
     * Returns the title from the underlying business object. If the object has
     * not yet been resolved the specification will be asked for a unresolved
     * title, which could of been persisted by the persistence mechanism. If
     * either of the above provides null as the title then this method will
     * return a title relating to the name of the object type, e.g. "A
     * Customer", "A Product".
     */
    public String titleString() {
        NakedObjectSpecification specification = getSpecification();
        String title = specification.getTitle().title(this);
        if (title == null && !isResolved()) {
            title = specification.unresolvedTitle(this);
        }
        if (title == null) {
            if (defaultTitle == null) {
                defaultTitle = "A " + specification.getSingularName().toLowerCase();
            }
            title = defaultTitle;
        }
        return title;
    }

    public String toString() {
        ToString str = new ToString(this);
        
        str.append(isPersistent() ? "P" : (isFinder() ? "F" : "T"));
        str.append(isResolved() ? "R" : "-");
        Oid oid = getOid();
        if (oid != null) {
            str.append(":");
            str.append(oid.toString().toUpperCase());
        } else {
            str.append(":-");
        }
        str.setAddComma();
        
        str.append("specification", specification == null ? "undetermined" : specification.getShortName());
        str.append("title", titleString());
        return str.toString();

        
        
        // speculating on performance.
        // For MemoryTestCase, fixture set up went from 1.502 secs to 1.291
        // secs.

        // return "POJO " + super.toString() +" " + specification == null ? "" :
        // titleString();
        //return "PojoAdapter ";
        //return "PojoAdapter " + titleString();
       //return "POJO " + super.toString() +" " + specification == null ? "" : titleString();
      //  return "POJO " + super.toString(); // +" "
                                                                         // +
                                                                         // specification
                                                                         // ==
                                                                         // null
                                                                         // ? ""
                                                                         // :
                                                                         // titleString();
    }

    protected void finalize() throws Throwable {
        super.finalize();
        //      LOG.info("finalizing pojo: " + pojo);
    }

    public void dispose() {
        pojo = null;
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