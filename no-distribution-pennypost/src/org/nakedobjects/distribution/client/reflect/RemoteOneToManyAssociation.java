package org.nakedobjects.distribution.client.reflect;

import org.nakedobjects.distribution.DistributionInterface;
import org.nakedobjects.distribution.ObjectReference;
import org.nakedobjects.distribution.RemoteObjectFactory;
import org.nakedobjects.distribution.SessionId;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.NotImplementedException;

import org.apache.log4j.Category;


public final class RemoteOneToManyAssociation implements OneToManyAssociation {
	private final static Category LOG = Category.getInstance(RemoteOneToManyAssociation.class);
	private OneToManyAssociation local;
	private boolean fullProxy = false;
	private DistributionInterface connection;
	private SessionId securityToken;
	private RemoteObjectFactory factory;

	public RemoteOneToManyAssociation(OneToManyAssociation local, final SessionId securityToken, final RemoteObjectFactory factory, final DistributionInterface connection) {
		this.local = local;
	   	this.securityToken = securityToken;
    	this.factory = factory;
    	this.connection = connection;
   }

    public void addAssociation(NakedObject inObject, NakedObject associate) {
        if (isPersistent(inObject)) {
	        LOG.debug("remote set association " + getName() + " in " + inObject + " with " + associate);
           try {
               ObjectReference targetReference = factory.createObjectReference(inObject);
               ObjectReference associateReference = factory.createObjectReference((NakedObject) associate);
               connection.associateObject(securityToken, targetReference, getName(), associateReference);
            } catch (NakedObjectRuntimeException e) {
                LOG.error("Problem with distribution ", e.getCause());
                throw (NakedObjectRuntimeException) e.getCause();
            }
        } else {
        	local.addAssociation(inObject, associate);
        }
    }

    public About getAbout(Session session, NakedObject inObject, NakedObject associate, boolean add) {
		if(inObject.getOid() != null && fullProxy) {
//    			try {
    			// TODO implement
    				throw new NotImplementedException();
//    				return new AboutFieldRequest(element, this).about();
  //  			} catch (ObjectStoreException e) {
//    				LOG.error("Problem with distribution", e.getCause());
//    				return null;
//    			}
        } else {
        	return local.getAbout(session, inObject, associate, add);
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

	public boolean hasAbout() {
		return local.hasAbout();
	}

	public boolean isDerived() {
		return local.isDerived();
	}
    
    public void removeAllAssociations(NakedObject inObject) {
    	local.removeAllAssociations(inObject);
	}
    
    /**
		Remove an associated object (the element) from the specified NakedObject in the association field represented by this object.
     */
    public void removeAssociation(NakedObject inObject, NakedObject associate) {

        if (isPersistent(inObject)  && fullProxy) {
	        LOG.debug("remote clear association " + inObject + "/" + associate);
            try {
                ObjectReference targetReference = factory.createObjectReference(inObject);
                ObjectReference associateReference = factory.createObjectReference((NakedObject) associate);
                connection.dissociateObject(securityToken, targetReference, getName(), associateReference);
            } catch (NakedObjectRuntimeException e) {
                LOG.error("Problem with distribution ", e.getCause());
                throw (NakedObjectRuntimeException) e.getCause();
            }
        } else {
        	local.removeAssociation(inObject, associate);
        }
    }

    private boolean isPersistent(NakedObject inObject) {
        return inObject.getOid() != null;
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
