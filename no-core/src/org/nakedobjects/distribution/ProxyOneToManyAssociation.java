package org.nakedobjects.distribution;

import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.OneToManyPeer;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.NotImplementedException;

import org.apache.log4j.Category;


public final class ProxyOneToManyAssociation implements OneToManyPeer {
    private final static Category LOG = Category.getInstance(ProxyOneToManyAssociation.class);
    private ClientDistribution connection;
    private boolean fullProxy = false;
    private OneToManyPeer local;

    public ProxyOneToManyAssociation(OneToManyPeer local, final ClientDistribution connection) {
        this.local = local;
        this.connection = connection;
    }

    public void addAssociation(NakedObject inObject, NakedObject associate) {
        if (isPersistent(inObject)) {
            LOG.debug("remote set association " + getName() + " in " + inObject + " with " + associate);
            try {
                connection.setAssociation(ClientSession.getSession(), getName(), inObject.getOid(), inObject.getSpecification().getFullName(), associate.getOid(), associate.getSpecification().getFullName());
            } catch (NakedObjectRuntimeException e) {
                LOG.error("Problem with distribution ", e.getCause());
                throw (NakedObjectRuntimeException) e.getCause();
            }
        } else {
            local.addAssociation(inObject, associate);
        }
    }


    public void initAssociation(NakedObject inObject, NakedObject associate) {
        local.initAssociation(inObject, associate);
    }
    
    public void initOneToManyAssociation(NakedObject inObject, NakedObject[] instances) {
        local.initOneToManyAssociation(inObject, instances);
    }

    public Hint getHint(Session session, NakedObject inObject, NakedObject associate, boolean add) {
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
            return local.getHint(session, inObject, associate, add);
        }
    }

    public NakedCollection getAssociations(NakedObject inObject) {
        if (isPersistent(inObject) && fullProxy) {
            LOG.debug("remote get association " + inObject);
            throw new NotImplementedException();
        } else {
            return local.getAssociations(inObject);
        }
    }

    public String getName() {
        return local.getName();
    }

    public NakedObjectSpecification getType() {
        return local.getType();
    }

    public boolean hasHint() {
        return local.hasHint();
    }

    public boolean isDerived() {
        return local.isDerived();
    }

    public boolean isEmpty(NakedObject inObject) {
        return local.isEmpty(inObject);
    }

    private boolean isPersistent(NakedObject inObject) {
        return inObject.getOid() != null;
    }

    public void removeAllAssociations(NakedObject inObject) {
        local.removeAllAssociations(inObject);
    }

    /**
     * Remove an associated object (the element) from the specified NakedObject
     * in the association field represented by this object.
     */
    public void removeAssociation(NakedObject inObject, NakedObject associate) {
        if (isPersistent(inObject) && fullProxy) {
            LOG.debug("remote clear association " + inObject + "/" + associate);
            try {
                connection.clearAssociation(ClientSession.getSession(), getName(), inObject.getOid(), inObject.getSpecification().getFullName(), associate.getOid(), associate.getSpecification().getFullName());
            } catch (NakedObjectRuntimeException e) {
                throw (NakedObjectRuntimeException) e.getCause();
            }
        } else {
            local.removeAssociation(inObject, associate);
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
