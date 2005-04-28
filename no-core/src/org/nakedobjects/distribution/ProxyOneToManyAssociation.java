package org.nakedobjects.distribution;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.AbstractOneToManyPeer;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.OneToManyPeer;
import org.nakedobjects.utility.NotImplementedException;

import org.apache.log4j.Category;


public final class ProxyOneToManyAssociation extends AbstractOneToManyPeer {
    private final static Category LOG = Category.getInstance(ProxyOneToManyAssociation.class);
    private ClientDistribution connection;
    private boolean fullProxy = false;

    public ProxyOneToManyAssociation(OneToManyPeer local, final ClientDistribution connection) {
        super(local);
        this.connection = connection;
    }

    public void addAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        if (isPersistent(inObject)) {
            LOG.debug("remote set association " + getName() + " in " + inObject + " with " + associate);
            try {
                connection.setAssociation(NakedObjects.getCurrentSession(), getName(), inObject.getOid(), inObject.getSpecification().getFullName(), associate.getOid(), associate.getSpecification().getFullName());
            } catch (NakedObjectRuntimeException e) {
                LOG.error("Problem with distribution ", e.getCause());
                throw (NakedObjectRuntimeException) e.getCause();
            }
        } else {
            super.addAssociation(identifier, inObject, associate);
        }
    }

    public Hint getHint(MemberIdentifier identifier, NakedObject inObject, NakedObject associate, boolean add) {
        if (inObject.getOid() != null && fullProxy) {
            //    			try {
            // TODO implement
            throw new NotImplementedException();
            //    				return new AboutFieldRequest(element, this).about();
            //  			} catch (ObjectStoreException e) {
            //    				LOG.error("Problem with distribution", e.getCause());
            //    				return null;
            //    			}
        } else {
            return super.getHint(identifier, inObject, associate, add);
        }
    }

    public NakedCollection getAssociations(MemberIdentifier identifier, NakedObject inObject) {
        if (isPersistent(inObject) && fullProxy) {
            LOG.debug("remote get association " + inObject);
            throw new NotImplementedException();
        } else {
            return super.getAssociations(identifier, inObject);
        }
    }

    private boolean isPersistent(NakedObject inObject) {
        return inObject.getOid() != null;
    }

    /**
     * Remove an associated object (the element) from the specified NakedObject
     * in the association field represented by this object.
     */
    public void removeAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        if (isPersistent(inObject) && fullProxy) {
            LOG.debug("remote clear association " + inObject + "/" + associate);
            try {
                connection.clearAssociation(NakedObjects.getCurrentSession(), getName(), inObject.getOid(), inObject.getSpecification().getFullName(), associate.getOid(), associate.getSpecification().getFullName());
            } catch (NakedObjectRuntimeException e) {
                throw (NakedObjectRuntimeException) e.getCause();
            }
        } else {
            super.removeAssociation(identifier, inObject, associate);
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
