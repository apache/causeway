package org.nakedobjects.object;

import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.ActionAbout;
import org.nakedobjects.object.control.ObjectAbout;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.utility.NotImplementedException;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.log4j.Logger;


public abstract class AbstractNakedObject implements NakedObject {
    private static final Logger LOG = Logger.getLogger(AbstractNakedObject.class);

    protected static NakedObject createInstance(Class type) {
        return createInstance(type.getName());
    }

    /**
	    A utility method for creating new objects in the context of the system - that is, it is added to the pool of
	    objects the enterprise system contains.
    */
    protected static NakedObject createInstance(String className) {
        NakedClass cls = NakedClassManager.getInstance().getNakedClass(className);
        NakedObject object;

        try {
            object = cls.acquireInstance();

            NakedObjectManager.getInstance().makePersistent(object);
            object.created();
            object.objectChanged();
        } catch (NakedObjectRuntimeException e) {
            object = new NakedError("Failed to create instance of " + cls);

            LOG.error("Failed to create instance of " + cls, e);
        }

        return object;
	}

    /**
       A utility method for creating new objects in the context of the system - that is, it is added to the pool of
       objects the enterprise system contains.
     */
    protected static NakedObject createTransientInstance(Class type) {
        return createTransientInstance(type.getName());
    }

    protected static NakedObject createTransientInstance(String className) {
        NakedClass nc = NakedClassManager.getInstance().getNakedClass(className);
        
        if (nc == null) {
            throw new RuntimeException("Invalid type to create " + className);
        }
       NakedObject object = nc.acquireInstance();

        object.created();

        return object;
    }

     /**
       A utiltiy method for simplifying the resolving of an objects attribute.  Calls resolve()
       on the secified object.  If the specified reference no action is done.
     */
    public static void resolve(NakedObject object) {
        if (object != null) {
            object.resolve();
        }
    }
    private boolean isFinder = false;
    private transient boolean isResolved = false;
    private Object oid;

    /**
       Return a standard READ/WRITE About, specifically: ObjectAbout.READ_WRITE
       @deprecated
     */
    public About about() {
    	return ObjectAbout.READ_WRITE;
    }
/*
	public void aboutExplorationActionClass(ActionAbout about) {
		about.unusableOnCondition(this instanceof NakedClass) || this instanceof InstanceCollection);
	}
*/
	public void aboutExplorationActionMakePersistent(ActionAbout about) {
		about.unusableOnCondition(isPersistent(), "Only non-persistent objects can be made persistent");
	}

    /**
       Copies the fields from the specified instance to the current instance.  Each NakedObject object reference
       is copied across and values for each NakedValue object are copied across to the NakedValue objects in the current
       instance.
     */
    public void copyObject(Naked objectToCopy) {
        if (objectToCopy.getClass() != getClass()) {
            throw new IllegalArgumentException("Copy object can only copy objects of the same type");
        }

        NakedObject object = (NakedObject) objectToCopy;
        Field[] fields = getNakedClass().getFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];

            if (field instanceof OneToManyAssociation) {
                ((NakedCollection) field.get(this)).copyObject((NakedCollection) field.get(object));
            } else if (field instanceof OneToOneAssociation) {
                ((OneToOneAssociation) field).initData(this, (NakedObject) field.get(object));
            } else {
                ((NakedValue) field.get(this)).copyObject((Naked) field.get(object));
            }
        }
    }

	/**
	 * hook method - fully specified in the interface
	 */
    public void created() {
    }

	/**
	 * hook method, see interface description for further details
	 */
    public void deleted() {
    }

    /**
     */
    public void destroy() throws ObjectStoreException {
        if (isPersistent()) {
            NakedObjectManager.getInstance().destroyObject(this);
        }
    }

    /**
       An object will be deemed to be equal if it: is this object; or has the same OID.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof AbstractNakedObject) {
            AbstractNakedObject ano = (AbstractNakedObject) obj;

            if (ano.getOid() == null) {
                return false;
            } else {
                return ano.getOid().equals(getOid());
            }
        } else {
            return false;
        }
    }

    public NakedClass explorationActionClass() {
        return getNakedClass();
    }

    /**
       Clones the current object.
     */
    public AbstractNakedObject explorationActionClone() {
        AbstractNakedObject clone = (AbstractNakedObject) createInstance(getClass());

        clone.copyObject(this);
        clone.objectChanged();

        return clone;
    }
    
    public NakedError explorationActionMakePersistent() {
    	makePersistent();
        return null;
    }

    /**
       Returns the short class name looking at the getFullClassName() result and
       passes back all the text after the last period/dot.
     */
    public String getShortClassName() {
        String name = getClassName();

        if (name.indexOf('$') >= 0) {
            name = name.substring(0, name.indexOf('$'));
        }

        return name.substring(name.lastIndexOf(".") + 1);
    }

    /**
       Returns the class name by getting the Class object fot this object and asks it for the name using  getName()
     */
    public String getClassName() {
        return this.getClass().getName();
    }

    /**
       Returns the String returned by getClassName()
       @see #getClassName
     */
    public String getIconName() {
        return getShortClassName();
    }

    public NakedClass getNakedClass() {
        return NakedClassManager.getInstance().getNakedClass(getClass().getName());
    }
    
    public Object getOid() {
        return oid;
    }

    public int hashCode() {
        if (getOid() == null) {
            return super.hashCode();
        } else {
            return (37 * 17) + getOid().hashCode();
        }
    }

    /**
       Returns false indicating that the object contains data.
     */
    public boolean isEmpty() {
        return false;
    }

    public boolean isFinder() {
        return isFinder;
    }

    /**
       Returns true if this object has an OID set.
     */
    public boolean isPersistent() {
        return getOid() != null;
    }

    public boolean isResolved() {
        return isResolved;
    }

    /**
     * returns true if the specified object is this object, i.e. no content comparison is done.
     * @see org.nakedobjects.object.NakedObject#isSameAs(Naked)
     */
    public boolean isSameAs(Naked object) {
        return object == this;
    }

    public void makeFinder() {
        if (isPersistent()) {
            throw new IllegalStateException("Can't make a persient object into a Finder");
        }

        isFinder = true;
    }

    public void makePersistent() {
        if (!isPersistent()) {
            LOG.debug("makePersistent(" + this + ")");
            NakedObjectManager.getInstance().makePersistent(this);
        }
    }

    /**
       Attempts to call <code>save</code> in the object store.
     */
    public void objectChanged() {
        LOG.debug("object changed " + this);
        if (isResolved()) {
            LOG.debug("  notifying object manager");
            NakedObjectManager.getInstance().objectChanged(this);
        } else if (isFinder()) {
            // if a finder then update the listeners
            
            //  need to update viewers somehow
        }
    }

    public synchronized void resolve() {
        if (!isResolved() && isPersistent()) {
        	NakedObjectManager.getInstance().resolve(this);
        }
    }

    public void setOid(Object oid) {
        if (this.oid == null) {
            this.oid = oid;
        } else {
            throw new IllegalStateException("The OID is already set (" + this + ")");
        }
    }

    public void setResolved() {
        if (isResolved) {
            throw new IllegalStateException("Object is already marked as resolved (" + this + ")");
        } else {
            isResolved = true;
        }
    }

    public Summary summary() {
        return new Summary();
    }

	/**
	 * every Naked Object is required to provide a <code>Title</code> by which 
	 * it is identified to the end user.
	 * <p>
	 * Unless overridden, the <code>String</code> representation of this 
	 * <code>Title</code> object is available through 
	 * <code>contextualTitle()</code>.
	 */
    public Title title() {
    	return new Title();
    }

    public String toString() {
        StringBuffer s = new StringBuffer();

        // datatype
        s.append(getShortClassName());
        s.append(" [");

        // type of object - EO, Primitive, Collection, with Status etc
        // Persistent/transient & Resolved or not
        s.append(isPersistent() ? "P" : (isFinder() ? "F" : "T"));
        s.append(isResolved ? "R" : "-");
        
        // obect identifier
        if (oid != null) {
            s.append(":");
            s.append(oid.toString().toUpperCase());
        } else {
            s.append(":-");
        }


        // title
        if(isResolved()) {
            s.append(" '");
            try {
                s.append(this.title());
            } catch (NullPointerException e) {
                s.append("no title");
            }
            s.append("'");
        } else {
            s.append(" unresolved");
        }
        s.append("]");
        
        s.append("  " + Long.toHexString(super.hashCode()).toUpperCase());

        return s.toString();
    }
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/