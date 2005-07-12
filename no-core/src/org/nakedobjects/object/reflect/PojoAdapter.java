package org.nakedobjects.object.reflect;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.ConcurrencyException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.utility.ToString;

import java.util.Date;

import org.apache.log4j.Logger;


public class PojoAdapter implements NakedObject {
    private final static Logger LOG = Logger.getLogger(PojoAdapter.class);
    private String defaultTitle;
    private Date modifiedTime;
    private String modifierBy;
    private Oid oid;
    private Object pojo;
    private transient ResolveState resolveState;
    private NakedObjectSpecification specification;
    private long version;

    public PojoAdapter(Object pojo) {
        this.pojo = pojo;
        resolveState = ResolveState.NEW;
    }

    public void checkLock(long version) {
        if (version != this.version) {
            throw new ConcurrencyException(modifierBy + " changed this object (" + titleString() + ") at " + modifiedTime);
        }
    }

    public void clearAssociation(NakedObjectAssociation specification, NakedObject associate) {
        resolveIfOnlyAGhost(this);
        LOG.debug("clearAssociation " + specification.getName() + "/" + associate + " in " + this);
        specification.clearAssociation(this, associate);
    }

    public void clearCollection(OneToManyAssociation association) {
        resolveIfOnlyAGhost(this);
        LOG.debug("clearCollection " + association.getName() + " in " + this);
        association.clearCollection(this);
    }

    public void clearValue(OneToOneAssociation association) {
        resolveIfOnlyAGhost(this);
        LOG.debug("clearValue " + association.getName() + " in " + this);
        association.clearValue(this);
    }

    public void debugClearResolved() {
        resolveState = ResolveState.NEW;
    }

    /**
     * Asks the reflector to tell the pojo that this object has been deleted.
     */
    public void destroyed() {
        resolveIfOnlyAGhost(this);
        LOG.debug("deleted notification for " + this);
        specification.deleted(this);
    }

    /**
     * Dissociates the POJO from this adapter.
     */
    public void dispose() {
        pojo = null;
    }

    public boolean equals(Object other) {
        return super.equals(other);
        /*
         * if (other == this) { return true; }
         * 
         * if (other instanceof PojoAdapter) { // we don't delegate to equals(PojoAdapter) because
         * we // don't want to do the identity test again. PojoAdapter otherPojoAdapter =
         * (PojoAdapter) other; return otherPojoAdapter.pojo == pojo; //
         * otherPojoAdapter.pojo.equals(pojo); } return false;
         */}

    /**
     * Overloaded to allow compiler to link directly if we know the compile-time type. (possible
     * performance improvement - called 166,000 times in normal ref data fixture. / public boolean
     * equals(PojoAdapter otherPojoAdapter) { if (otherPojoAdapter == this) { return true; } return
     * otherPojoAdapter.pojo == pojo; // otherPojoAdapter.pojo.equals(pojo); }
     */

    public Naked execute(Action action, Naked[] parameters) {
        resolveIfOnlyAGhost(this);
        LOG.debug("execute " + action.getName() + " in " + this);
        for (int i = 0; parameters != null && i < parameters.length; i++) {
            if (parameters[i] instanceof NakedObject) {
                resolveIfOnlyAGhost((NakedObject) parameters[i]);
            }
        }
        Naked result = action.execute(this, parameters);
        return result;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        LOG.debug("finalizing pojo: " + pojo);
    }

    public NakedObject getAssociation(OneToOneAssociation field) {
        resolveIfOnlyAGhost(this);
        return (NakedObject) field.get(this);
    }

    public Naked getField(NakedObjectField field) {
        resolveIfOnlyAGhost(this);
        return field.get(this);
    }

    public NakedObjectField[] getFields() {
        return getSpecification().getFields();
    }

    public Hint getHint(Action action, Naked[] parameterValues) {
        resolveIfOnlyAGhost(this);
        return action.getHint(this, parameterValues);
    }

    public Hint getHint(NakedObjectField field, Naked value) {
        resolveIfOnlyAGhost(this);
        if (field instanceof OneToOneAssociation) {
            return ((OneToOneAssociation) field).getHint(this, value);
        } else if (field instanceof OneToManyAssociation) {
            return ((OneToManyAssociation) field).getHint(this);
        } else {
            throw new NakedObjectRuntimeException();
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

    public String getLabel(Action action) {
        return action.getLabel(this);
    }

    public String getLabel(NakedObjectField field) {
        return field.getLabel(this);
    }

    public Object getObject() {
        return pojo;
    }

    public Oid getOid() {
        return oid;
    }

    public ActionParameterSet getParameters(Action action) {
        return action.getParameters(this);
    }

    public NakedObjectSpecification getSpecification() {
        if (specification == null) {
            specification = NakedObjects.getSpecificationLoader().loadSpecification(getObject().getClass());
        }
        return specification;
    }

    public NakedValue getValue(OneToOneAssociation field) {
        resolveIfOnlyAGhost(this);
        return (NakedValue) field.get(this);
    }

    public long getVersion() {
        return version;
    }

    public NakedObjectField[] getVisibleFields() {
        return getSpecification().getVisibleFields(this);
    }

    public void initAssociation(NakedObjectAssociation field, NakedObject associatedObject) {
        LOG.debug("initAssociation " + field.getName() + "/" + associatedObject + " in " + this);
        field.initAssociation(this, associatedObject);
    }

    public void initOneToManyAssociation(OneToManyAssociation field, NakedObject[] instances) {
        LOG.debug("initAssociation " + field.getName() + " with " + instances.length + "instances in " + this);
        field.initOneToManyAssociation(this, instances);
    }

    public void initValue(OneToOneAssociation field, Object object) {
        LOG.debug("initValue " + field.getName() + " with " + object + " in " + this);
        field.initValue(this, object);
    }

    public boolean isEmpty(NakedObjectField field) {
        resolveIfOnlyAGhost(this);
        return field.isEmpty(this);
    }

    private boolean isResolved() {
        return resolveState == ResolveState.RESOLVED;
    }

    private void resolveIfOnlyAGhost(NakedObject object) {
        ResolveState resolveState = object.getResolveState();
        if (resolveState.isGhost()) {
            LOG.info("Unresolved object attempting to be used; resolving it immediately: " + object);
            NakedObjects.getObjectManager().resolveImmediately(object);
        }
    }

    public Persistable persistable() {
        return getSpecification().persistable();
    }

    public void persistedAs(Oid oid) {
        LOG.debug("set OID " + oid + " " + this);
        Assert.assertTrue("Cannot make a non-transient object persistent", this, getResolveState().isTransient());
        Assert.assertTrue("Oid can't be set again", this, getOid() == null);

        this.oid = oid;
        resolveState = ResolveState.RESOLVED;
    }

    public void setAssociation(NakedObjectAssociation field, NakedObject associatedObject) {
        resolveIfOnlyAGhost(this);
        LOG.debug("setAssociation " + field.getName() + " with " + associatedObject + " in " + this);
        field.setAssociation(this, associatedObject);
    }

    public void setOptimisticLock(long version, String modifierBy, Date modifiedTime) {
        this.version = version;
        this.modifierBy = modifierBy;
        this.modifiedTime = modifiedTime;
    }

    public void setValue(OneToOneAssociation field, Object object) {
        resolveIfOnlyAGhost(this);
        LOG.debug("setValue " + field.getName() + " with " + object + " in " + this);
        field.setValue(this, object);
    }

    /**
     * Returns the title from the underlying business object. If the object has not yet been
     * resolved the specification will be asked for a unresolved title, which could of been
     * persisted by the persistence mechanism. If either of the above provides null as the title
     * then this method will return a title relating to the name of the object type, e.g. "A
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

    public synchronized String toString() {
        ToString str = new ToString(this);

   /*     if (isUnresolved()) {
            str.append("-");
        } else if (isPersistent()) {
            str.append("P");
        } else {
            str.append("T");
        }
*/
        str.append(resolveState.code());
        Oid oid = getOid();
        if (oid != null) {
            str.append(":");
            str.append(oid.toString().toUpperCase());
        } else {
            str.append(":-");
        }
        str.setAddComma();

        str.append("specification", specification == null ? "undetermined" : specification.getShortName());
        if (isResolved()) {
            // don't do title of unresolved objects as this may force the resolving of the object.
            str.append("title", titleString());
        }
        str.appendAsHex("pojo-hash", pojo.hashCode());
        return str.toString();
    }

    public void recreatedAs(Oid oid) {
        changeState(ResolveState.GHOST);
        this.oid = oid;
    }

    public void changeState(ResolveState newState) {
        Assert.assertTrue("can't change from " + resolveState.name() + " to " + newState.name() + ": " + this, resolveState
                .isValidToChangeTo(newState));
        LOG.debug("recreate - change state " + this + " to " + newState);
        resolveState = newState;
    }

    public ResolveState getResolveState() {
        return resolveState;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */