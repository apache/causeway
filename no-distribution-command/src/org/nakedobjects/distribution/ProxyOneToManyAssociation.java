package org.nakedobjects.distribution;

import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.reflect.AbstractOneToManyPeer;
import org.nakedobjects.object.reflect.OneToManyPeer;
import org.nakedobjects.utility.NakedObjectRuntimeException;
import org.nakedobjects.utility.NotImplementedException;

import org.apache.log4j.Category;


public final class ProxyOneToManyAssociation extends AbstractOneToManyPeer {
    private final static Category LOG = Category.getInstance(ProxyOneToManyAssociation.class);
    private Distribution connection;
    private boolean fullProxy = false;
    private final DataFactory objectDataFactory;

    public ProxyOneToManyAssociation(OneToManyPeer local, final Distribution connection, DataFactory objectDataFactory) {
        super(local);
        this.connection = connection;
        this.objectDataFactory = objectDataFactory;
    }

    public void addAssociation(NakedObject inObject, NakedObject associate) {
        if (isPersistent(inObject)) {
            LOG.debug("set association remotely " + getIdentifier() + " in " + inObject + " with " + associate);
            try {
                ReferenceData targetReference = objectDataFactory.createReference(inObject);
                ReferenceData associateReference = objectDataFactory.createReference(associate);
                connection.setAssociation(NakedObjects.getCurrentSession(), getIdentifier().getName(), targetReference, associateReference);
            } catch (NakedObjectRuntimeException e) {
                LOG.error("problem with distribution ", e.getCause());
                throw (NakedObjectRuntimeException) e.getCause();
            }
        } else {
            LOG.debug("set association locally " + getIdentifier() + " in " + inObject + " with " + associate);
            super.addAssociation(inObject, associate);
        }
    }

    public NakedCollection getAssociations(NakedObject inObject) {
        if (isPersistent(inObject) && fullProxy) {
            LOG.debug("get association remotely " + inObject);
            throw new NotImplementedException();
        } else {
            LOG.debug("get association locally " + inObject);
            return super.getAssociations(inObject);
        }
    }

    private boolean isPersistent(NakedObject inObject) {
        return inObject.getOid() != null;
    }

    /**
     * Remove an associated object (the element) from the specified NakedObject in the association
     * field represented by this object.
     */
    public void removeAssociation(NakedObject inObject, NakedObject associate) {
        if (isPersistent(inObject)) {
            LOG.debug("clear association remotely " + inObject + "/" + associate);
            try {
                ReferenceData targetReference = objectDataFactory.createReference(inObject);
                ReferenceData associateReference = objectDataFactory.createReference(associate);
                connection.clearAssociation(NakedObjects.getCurrentSession(), getIdentifier().getName(), targetReference, associateReference);
            } catch (NakedObjectRuntimeException e) {
                throw (NakedObjectRuntimeException) e.getCause();
            }
        } else {
            LOG.debug("clear association locally " + inObject + "/" + associate);
            super.removeAssociation(inObject, associate);
        }
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
