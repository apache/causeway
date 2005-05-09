package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.defaults.AbstractNakedObject;


public class PojoAdapter extends AbstractNakedObject {
    private Object pojo;

    protected PojoAdapter(Object pojo) {
        this.pojo = pojo;
    }

    public void clearAssociation(NakedObjectAssociation specification, NakedObject associate) {
        specification.clearAssociation(this, associate);
    }

    public void clearCollection(OneToManyAssociation association) {
        association.clearCollection(this);
    }

    public void clearValue(OneToOneAssociation association) {
        association.clearValue(this);
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (other instanceof PojoAdapter) {
			// we don't delegate to equals(PojoAdapter) because we
			// don't want to do the identity test again.
			PojoAdapter otherPojoAdapter = (PojoAdapter)other;
			return otherPojoAdapter.pojo == pojo; // otherPojoAdapter.pojo.equals(pojo);
		}
        return false;
    }

	/**
	 * Overloaded to allow compiler to link directly if we know the compile-time type.
	 * (possible performance improvement - called 166,000 times in normal ref data fixture.
	 */
	public boolean equals(PojoAdapter otherPojoAdapter) {
		if (otherPojoAdapter == this) {
			return true;
		}
		return otherPojoAdapter.pojo == pojo; // otherPojoAdapter.pojo.equals(pojo);
	}

	public Naked execute(Action action, Naked[] parameters) {
        Naked result = action.execute(this, parameters);
        return result;
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

    public NakedObjectField[] getVisibleFields() {
        return getSpecification().getVisibleFields(this);
    }

    
    public Hint getHint(Action action, Naked[] parameterValues) {
        return action.getHint(this, parameterValues);
    }

    public Hint getHint(NakedObjectField field, Naked value) {
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
        return (NakedValue) field.get(this);
    }

    public void initAssociation(NakedObjectAssociation field, NakedObject associatedObject) {
        field.initAssociation(this, associatedObject);
    }

    public void initOneToManyAssociation(OneToManyAssociation field, NakedObject[] instances) {
        field.initOneToManyAssociation(this, instances);
    }

    public void initValue(OneToOneAssociation field, Object object) {
        field.initValue(this, object);
    }

    public boolean isEmpty(NakedObjectField field) {
        return field.isEmpty(this);
    }
    
    public Persistable persistable() {
        return getSpecification().persistable();
    }

    public boolean isParsable() {
        return getSpecification().isParsable();
    }

    public void setAssociation(NakedObjectAssociation field, NakedObject associatedObject) {
        field.setAssociation(this, associatedObject);
    }

    public void setValue(OneToOneAssociation field, Object object) {
        field.setValue(this, object);
    }

	/**
	 * Introduced during performance profiling; PojoAdapter#titleString often called.
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
		// speculating on performance.  
		// For MemoryTestCase, fixture set up went from 1.502 secs to 1.291 secs.

        // return "POJO " + super.toString() +" " + specification == null ? "" : titleString();
		return "POJO " + super.toString(); // +" " + specification == null ? "" : titleString();
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