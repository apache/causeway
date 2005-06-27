package org.nakedobjects.distribution;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.AbstractOneToOnePeer;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.OneToOnePeer;

import org.apache.log4j.Logger;


public final class ProxyOneToOneAssociation extends AbstractOneToOnePeer {
    private final static Logger LOG = Logger.getLogger(ProxyOneToOneAssociation.class);
    private final ClientDistribution connection;
    private final boolean fullProxy = false;

    public ProxyOneToOneAssociation(OneToOnePeer local, final ClientDistribution connection) {
        super(local);
        this.connection = connection;
    }

    public void clearAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        if (isPersistent(inObject)) {
            LOG.debug("clear association remotely " + inObject + "/" + associate);
            connection.clearAssociation(NakedObjects.getCurrentSession(), getName(), inObject.getOid(), inObject
                    .getSpecification().getFullName(), associate.getOid(), associate.getSpecification().getFullName());
        } else {
            LOG.debug("clear association locally " + inObject + "/" + associate);
            super.clearAssociation(identifier, inObject, associate);
        }
    }

    public Naked getAssociation(MemberIdentifier identifier, NakedObject inObject) {
        if (isPersistent(inObject) && fullProxy) {
            //  return
            // connection.getOneToOneAssociation(ClientSession.getSession(),
            // inObject);
            throw new NotExpectedException();
        } else {
            return super.getAssociation(identifier, inObject);
        }
    }

    public Hint getHint(MemberIdentifier identifier, NakedObject inObject, Naked associate) {
        if (isPersistent(inObject) && fullProxy) {
            throw new NotExpectedException();
        } else {
            return super.getHint(identifier, inObject, associate);
        }
    }

    private boolean isPersistent(NakedObject inObject) {
        return inObject.getOid() != null;
    }

    public void setAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        if (isPersistent(inObject)) {
            LOG.debug("set association remotely " + getName() + " in " + inObject + " with " + associate);
            connection.setAssociation(NakedObjects.getCurrentSession(), getName(), inObject.getOid(), inObject.getSpecification()
                    .getFullName(), associate.getOid(), associate.getSpecification().getFullName());
        } else {
            LOG.debug("set association locally " + getName() + " in " + inObject + " with " + associate);
            super.setAssociation(identifier, inObject, associate);
        }
    }

    public void setValue(MemberIdentifier identifier, NakedObject inObject, Object value) {
        if (isPersistent(inObject)) {
            LOG.debug("set value remotely " + getName() + " in " + inObject + " with " + value);
            connection.setValue(NakedObjects.getCurrentSession(), getName(), inObject.getOid(), inObject.getSpecification()
                    .getFullName(), value);
        } else {
            LOG.debug("set value locally " + getName() + " in " + inObject + " with " + value);
            super.setValue(identifier, inObject, value);
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