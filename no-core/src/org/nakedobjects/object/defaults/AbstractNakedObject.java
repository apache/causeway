package org.nakedobjects.object.defaults;

import org.nakedobjects.object.ArbitraryNakedCollection;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.control.ActionAbout;
import org.nakedobjects.object.control.FieldAbout;
import org.nakedobjects.object.defaults.collection.ArbitraryCollectionVector;
import org.nakedobjects.object.defaults.collection.InternalCollectionVector;
import org.nakedobjects.object.defaults.value.Date;
import org.nakedobjects.object.defaults.value.TimeStamp;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.reflect.OneToManyAssociationSpecification;
import org.nakedobjects.object.reflect.OneToOneAssociationSpecification;

import org.apache.log4j.Logger;


public abstract class AbstractNakedObject implements NakedObject {
    private static final Logger LOG = Logger.getLogger(AbstractNakedObject.class);

    /**
     * A utility method for creating new objects in the context of the system -
     * that is, it is added to the pool of objects the enterprise system
     * contains.
     */
    protected NakedObject createInstance(Class cls) {
        return getObjectManager().createInstance(cls.getName());
    }

    /**
     * A utility method for creating new objects in the context of the system -
     * that is, it is added to the pool of objects the enterprise system
     * contains.
     */
    protected NakedObject createInstance(String className) {
        return getObjectManager().createInstance(className);
    }

    /**
     * A utility method for creating new objects, which are transient and will not 
     * be added to the pool of objects the enterprise system
     * contains.
     */
    protected NakedObject createTransientInstance(Class type) {
        return getObjectManager().createTransientInstance(type.getName());
    }

    /**
     * A utility method for creating new objects, which are transient and will not 
     * be added to the pool of objects the enterprise system
     * contains.
     */
    protected NakedObject createTransientInstance(String className) {
        return getObjectManager().createTransientInstance(className);
    }

    /**
     * A utility method for creating a new internal collection object for storing one-to-many
     * associations.
     */
    protected ArbitraryNakedCollection createArbitraryCollection(String name) {
        return new ArbitraryCollectionVector(name);
    }
    

    /**
     * A utility method for creating a new internal collection object for storing one-to-many
     * associations.
     */
    protected InternalCollection createInternalCollection(Class elementType) {
        return new InternalCollectionVector(elementType, this);
    }
    
    /**
     * A utility method for creating a new internal collection object for storing one-to-many
     * associations.
     */
    protected InternalCollection createInternalCollection(String elementType) {
        return  new InternalCollectionVector(elementType, this);
    }
        
    /**
     * A utiltiy method for simplifying the resolving of an objects attribute.
     * Calls resolve() on the secified object. If the specified reference no
     * action is done.
     */
    
    protected void resolve(NakedObject object) {
        if (object != null && ! object.isResolved()) {
            getObjectManager().resolve(object);
        }
    }

    private final Date dateCreated = new Date();
    private boolean isFinder = false;
    private transient boolean isResolved = false;
    private TimeStamp lastActivity = new TimeStamp();
    private Oid oid;
    private NakedObjectContext context;
    private NakedObjectSpecification specification;
    
    public void setContext(NakedObjectContext context) {
        this.context = context;
    }
    
    public void setNakedClass(NakedObjectSpecification nakedClass) {
        this.specification = nakedClass;
    }
    
    public AbstractNakedObject() {
        lastActivity.clear();
    }

    /*
     * public void aboutExplorationActionClass(ActionAbout about) {
     * about.unusableOnCondition(this instanceof NakedClass) || this instanceof
     * InstanceCollection); }
     */
    public void aboutActionPersist(ActionAbout about) {
        if (isPersistent()) {
            about.invisible();
        }
    }

    public final void aboutDateCreated(FieldAbout about) {
        if(showDateCreated()) {
            about.unmodifiable();
        } else {
            about.invisible();
        }
    }

    protected boolean showDateCreated() {
        return false;
    }

    public final void aboutLastActivity(FieldAbout about) {
        if(showLastActivity()) {
            about.unmodifiable();
        } else {
            about.invisible();
        }
    }

    protected boolean showLastActivity() {
        return false;
    }

    public NakedError actionPersist() {
        makePersistent();
        return null;
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
        FieldSpecification[] fields = getSpecification().getFields();

        for (int i = 0; i < fields.length; i++) {
            FieldSpecification field = fields[i];

            if (field instanceof OneToManyAssociationSpecification) {
                ((NakedCollection) field.get(this)).copyObject((NakedCollection) field.get(object));
            } else if (field instanceof OneToOneAssociationSpecification) {
                ((OneToOneAssociationSpecification) field).initData(this, (NakedObject) field.get(object));
            } else {
                ((NakedValue) field.get(this)).copyObject((Naked) field.get(object));
            }
        }
    }

    /**
     * hook method - fully specified in the interface
     */
    public void created() {}

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

    public NakedClass debugActionClass() {
        return getObjectManager().getNakedClass(getSpecification());
    }

    /**
     * Clones the current object.
     */
    public AbstractNakedObject debugActionClone() {
        AbstractNakedObject clone = (AbstractNakedObject) createInstance(getClass());

        clone.copyObject(this);
        clone.objectChanged();

        return clone;
    }

     public NakedObjectContext getContext() {
         // TODO context needs to assigned to the object properly
//        Assert.assertTrue("must have a context: " + this, context != null);
		 if(context == null) context = NakedObjectContext.getDefaultContext();
        return context;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * Returns the short name from this objects NakedObjectSpecification
     */
    public String getIconName() {
        return null;
    }

    public TimeStamp getLastActivity() {
        return lastActivity;
    }

    public NakedObjectSpecification getSpecification() {
        if(specification == null) {
            specification = NakedObjectSpecificationLoader.getInstance().loadSpecification(this.getClass());
        }
        return specification;
    }

    protected NakedObjectManager getObjectManager() {
        return getContext().getObjectManager();
    }
    
    public Oid getOid() {
        return oid;
    }

    /**
     * Returns false indicating that the object contains data.
     */
    public boolean isEmpty() {
        return false;
    }

    public boolean isFinder() {
        return isFinder;
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

    /**
     * Attempts to call <code>save</code> in the object store.
     * When the state of this object changes, e.g., an attribute is set, then
     * this method should be called so that it is persisted and a message is
     * propogated to the users of this object within the system.
     */
    public void objectChanged() {
        LOG.debug("object changed " + this);
        if (isResolved()) {
            LOG.debug("  notifying object manager");
            getObjectManager().objectChanged(this);
        }
    }

    /**
     * Resolves the current object ensuring all its attributes are available in
     * memory.
     */
    public synchronized void resolve() {
        if (!isResolved() && isPersistent()) {
            getObjectManager().resolve(this);
        }
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

    public String titleString() {
        return title().toString();
    }
    
    /**
     * every Naked Object is required to provide a <code>Title</code> by which
     * it is identified to the end user.
     * <p>
     * Unless overridden, the <code>String</code> representation of this
     * <code>Title</code> object is available through
     * <code>contextualTitle()</code>.
     */
    protected Title title() {
        return new Title();
    }

    public String toString() {
        StringBuffer s = new StringBuffer();

        // datatype
        NakedObjectSpecification spec = getSpecification();
        s.append(spec == null ? getClass().getName() : spec.getShortName());
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
        if (isResolved()) {
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
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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