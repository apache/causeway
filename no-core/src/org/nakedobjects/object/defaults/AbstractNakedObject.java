package org.nakedobjects.object.defaults;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.persistence.ObjectStoreException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;

import org.apache.log4j.Logger;


public abstract class AbstractNakedObject implements NakedObject {
    private static final Logger LOG = Logger.getLogger(AbstractNakedObject.class);
    private boolean isFinder = false;
    private boolean isPersistDirty;
    private transient boolean isResolved = false;
    private boolean isViewDirty;
    private Oid oid;
    private NakedObjectSpecification specification;

    public NakedError actionPersist() {
        makePersistent();
        return null;
    }
     

     private void checkIsPojoDirty() {
        boolean isDirty = getSpecification() == null ? false : getSpecification().isDirty(this);
        if(isDirty) {
            markDirty();
        }
     }

    public void clearPersistDirty() {
        isPersistDirty = false;
    }

    public void clearViewDirty() {
        isViewDirty = false;
    }
 
    /**
     * Copies the fields from the specified instance to the current instance.
     * Each NakedObject object reference is copied across and values for each
     * NakedValue object are copied across to the NakedValue objects in the
     * current instance.
     */
    public void copyObject(Naked objectToCopy) {
        if (objectToCopy.getClass() != getClass()) {
            throw new IllegalArgumentException("Copy object can only copy objects of the same type");
        }

        NakedObject object = (NakedObject) objectToCopy;
        NakedObjectField[] fields = getSpecification().getFields();

        for (int i = 0; i < fields.length; i++) {
            NakedObjectField field = fields[i];

            if (field instanceof OneToManyAssociation) {
                getField(field).copyObject(object.getField(field));
            } else if (field instanceof OneToOneAssociation) {
                setValue((OneToOneAssociation) field, (NakedObject) object.getField(field));
            } else {
  //              ((NakedValue) field.get(this)).copyObject((Naked) field.get(object));
            }
        }
    }

     public void created() {}
    /**
     * A utility method for creating new objects in the context of the system -
     * that is, it is added to the pool of objects the enterprise system
     * contains.
     */
    protected NakedObject createInstance(Class cls) {
        return getObjectManager().createInstance(cls.getName());
    }

    /**
     * hook method, see interface description for further details
     */
    public void deleted() {}

    /**
     */
    protected void destroy() throws ObjectStoreException {
        if (isPersistent()) {
            getObjectManager().destroyObject(this);
        }
    }

    /**
     * An object will be deemed to be equal if it: is this object; or has the
     * same OID.
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

     /**
     * Returns the short name from this objects NakedObjectSpecification
     * 
     * TODO allow the reflector to set up a icon name
     */
    public String getIconName() {
        return null;
    }

    public Object getObject() {
        return this;
    }
    
    public Oid getOid() {
        return oid;
    }

    public NakedObjectSpecification getSpecification() {
        if(specification == null) {
            //specification = NakedObjectSpecificationLoader.getInstance().loadSpecification(this.getClass());
            specification = NakedObjectSpecificationLoader.getInstance().loadSpecification(getObject().getClass());
        }
        return specification;
    }

    /**
     * Returns false indicating that the object contains data.
     */
    public boolean isEmpty(NakedObjectField field) {
        return false;
    }

    public boolean isFinder() {
        return isFinder;
    }
     
     public boolean isPersistDirty() {
         checkIsPojoDirty();
         return isPersistDirty;
     }

    /**
     * Returns true if this object has an OID set.
     */
    public boolean isPersistent() {
        return getOid() != null;
    }

    public boolean isResolved() {
        return isResolved;
    }

    /**
     * returns true if the specified object is this object, i.e. no content
     * comparison is done.
     * 
     * @see org.nakedobjects.object.NakedObject#isSameAs(Naked)
     */
    public boolean isSameAs(Naked object) {
        return object == this;
    }
     
     public boolean isViewDirty() {
         checkIsPojoDirty();
         return isViewDirty;
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
            getObjectManager().makePersistent(this);
        }
    }


    public void markDirty() {
        isViewDirty = true;
        isPersistDirty = true;
        getSpecification().clearDirty(this);
    }

    /**
     * Attempts to call <code>save</code> in the object store.
     * When the state of this object changes, e.g., an attribute is set, then
     * this method should be called so that it is persisted and a message is
     * propogated to the users of this object within the system.
     */
 /*   public void objectChanged() {
        LOG.debug("object changed " + this);
        if (isResolved()) {
            LOG.debug("  notifying object manager");
            getObjectManager().objectChanged(this);
        }
    }
*/
    /**
     * Resolves the current object ensuring all its attributes are available in
     * memory.
     */
    public synchronized void resolve() {
        if (!isResolved() && isPersistent()) {
            getObjectManager().resolveImmediately(this);
        }
    }

    private NakedObjectManager getObjectManager() {
        return NakedObjects.getObjectManager();
    }


    /**
     * A utiltiy method for simplifying the resolving of an objects attribute.
     * Calls resolve() on the secified object. If the specified reference no
     * action is done.
     */
    
    protected void resolve(NakedObject object) {
        if (object != null && ! object.isResolved()) {
            getObjectManager().resolveImmediately(object);
        }
    }
    
    
    public void setNakedClass(NakedObjectSpecification nakedClass) {
        this.specification = nakedClass;
    }

    public void setOid(Oid oid) {
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
    
     public String toString() {
        StringBuffer s = new StringBuffer();

        // datatype
        NakedObjectSpecification spec = getSpecification();
        s.append(spec == null ? getClass().getName() : spec.getShortName());
        s.append(" [");

        // type of object - EO, Primitive, Collection, with Status etc
        // Persistent/transient & Resolved or not
        s.append(isViewDirty ? "V" : "-");
        s.append(isPersistDirty ? "D" : "-");
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
        if (isResolved()) {
            s.append(" '");
            try {
                s.append(this.titleString());
            } catch (NullPointerException e) {
                s.append("no title");
            }
            s.append("'");
        }
        s.append("]");

        s.append("  " + Long.toHexString(super.hashCode()).toUpperCase());

        return s.toString();
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