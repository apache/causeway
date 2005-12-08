package org.nakedobjects.distribution;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.reflect.AbstractOneToOnePeer;
import org.nakedobjects.object.reflect.OneToOnePeer;

import org.apache.log4j.Logger;


public final class ProxyOneToOneAssociation extends AbstractOneToOnePeer {
    private final static Logger LOG = Logger.getLogger(ProxyOneToOneAssociation.class);
    private final Distribution connection;
    private final boolean fullProxy = false;
    private final ObjectEncoder objectDataFactory;

    public ProxyOneToOneAssociation(OneToOnePeer local, final Distribution connection, ObjectEncoder objectDataFactory) {
        super(local);
        this.connection = connection;
        this.objectDataFactory = objectDataFactory;
    }

    public void clearAssociation(NakedObject inObject, NakedObject associate) {
        if (isPersistent(inObject)) {
            LOG.debug("clear association remotely " + inObject + "/" + associate);
            ReferenceData targetReference = objectDataFactory.createReference(inObject);
            ReferenceData associateReference = objectDataFactory.createReference(associate);
            connection.clearAssociation(NakedObjects.getCurrentSession(), getIdentifier().getName(), targetReference, associateReference);
        } else {
            LOG.debug("clear association locally " + inObject + "/" + associate);
            super.clearAssociation(inObject, associate);
        }
    }

    public Naked getAssociation(NakedObject inObject) {
        if (isPersistent(inObject) && fullProxy) {
            //  return
            // connection.getOneToOneAssociation(ClientSession.getSession(),
            // inObject);
            throw new NotExpectedException();
        } else {
            return super.getAssociation(inObject);
        }
    }

    private boolean isPersistent(NakedObject inObject) {
        return inObject.getOid() != null;
    }

    public void setAssociation(NakedObject inObject, NakedObject associate) {
        if (isPersistent(inObject)) {
            LOG.debug("set association remotely " + getIdentifier() + " in " + inObject + " with " + associate);
            ReferenceData targetReference = objectDataFactory.createReference(inObject);
            ReferenceData associateReference = objectDataFactory.createReference(associate);
            connection.setAssociation(NakedObjects.getCurrentSession(), getIdentifier().getName(), targetReference, associateReference);
        } else {
            LOG.debug("set association locally " + getIdentifier() + " in " + inObject + " with " + associate);
            super.setAssociation(inObject, associate);
        }
    }

    public void setValue(NakedObject inObject, Object value) {
        if (isPersistent(inObject)) {
            LOG.debug("set value remotely " + getIdentifier() + " in " + inObject + " with " + value);
            ReferenceData targetReference = objectDataFactory.createReference(inObject);
            connection.setValue(NakedObjects.getCurrentSession(), getIdentifier().getName(), targetReference, value);
        } else {
            LOG.debug("set value locally " + getIdentifier() + " in " + inObject + " with " + value);
            super.setValue(inObject, value);
        }
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