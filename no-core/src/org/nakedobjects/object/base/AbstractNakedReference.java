package org.nakedobjects.object.base;

import org.nakedobjects.object.ConcurrencyException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.NakedReference;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.Version;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.utility.ToString;

import java.text.DateFormat;

import org.apache.log4j.Logger;


public abstract class AbstractNakedReference implements NakedReference {
    private final static Logger LOG = Logger.getLogger(AbstractNakedReference.class);
    private final static DateFormat DATE_TIME = DateFormat.getDateTimeInstance();
    private Oid oid;
    private transient ResolveState resolveState;
    private NakedObjectSpecification specification;
    private Version version;
    private String defaultTitle;
    
    public AbstractNakedReference() {
        resolveState = ResolveState.NEW;
    }

    public void changeState(ResolveState newState) {
        Assert.assertTrue("can't change from " + resolveState.name() + " to " + newState.name() + ": " + this, resolveState
                .isValidToChangeTo(newState));
        LOG.debug("recreate - change state " + this + " to " + newState);
        resolveState = newState;
    }

    public void checkLock(Version version) {
        if (this.version.different(version)) {
            throw new ConcurrencyException(version.getUser() + " changed "
                    + titleString() + " at " + DATE_TIME.format(version.getTime()) + " (" + this.version + "~" + version + ")");
        }
    }

    public void debugClearResolved() {
        resolveState = ResolveState.GHOST;
    }

    /**
     * Returns the short name from this objects NakedObjectSpecification
     * 
     * TODO allow the reflector to set up a icon name
     */
    public String getIconName() {
        return null;
    }

    public Oid getOid() {
        return oid;
    }

    public ResolveState getResolveState() {
        return resolveState;
    }

    public NakedObjectSpecification getSpecification() {
        if (specification == null) {
            specification = NakedObjects.getSpecificationLoader().loadSpecification(getObject().getClass());
            defaultTitle = "A " + specification.getSingularName().toLowerCase();
        }
        return specification;
    }

    public Version getVersion() {
        return version;
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }
    
    protected boolean isResolved() {
        return resolveState == ResolveState.RESOLVED;
    }

    public Persistable persistable() {
        return getSpecification().persistable();
    }

    public void persistedAs(Oid oid) {
        LOG.debug("set OID " + oid + " " + this);
        Assert.assertTrue("Cannot make a non-transient object persistent", this, getResolveState().isTransient());
        Assert.assertTrue("Oid can't be set again", this, getOid() == null);

        setOid(oid);
        setResolveState(ResolveState.RESOLVED);
    }

    protected void setOid(Oid oid) {
        this.oid = oid;
    }

    public void setOptimisticLock(Version version) {
        if (shouldSetVersion(version)) {
            this.version = version;
        }
    }

    private boolean shouldSetVersion(Version version) {
        return this.version == null || version.different(this.version);
    }

    protected void setResolveState(ResolveState resolveState) {
        this.resolveState = resolveState;
    }

    protected void toString(ToString str) {
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
        str.append("version", version);
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