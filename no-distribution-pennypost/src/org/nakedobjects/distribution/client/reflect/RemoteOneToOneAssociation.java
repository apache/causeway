package org.nakedobjects.distribution.client.reflect;

import org.nakedobjects.distribution.DistributionInterface;
import org.nakedobjects.distribution.ObjectData;
import org.nakedobjects.distribution.ObjectReference;
import org.nakedobjects.distribution.RemoteObjectFactory;
import org.nakedobjects.distribution.SessionId;
import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.security.Session;

import org.apache.log4j.Category;


public final class RemoteOneToOneAssociation implements OneToOneAssociation {
	private final static Category LOG = Category.getInstance(RemoteOneToOneAssociation.class);
	private OneToOneAssociation local;
	private final boolean fullProxy = false;
	private final DistributionInterface connection;
	private final SessionId securityToken;
	private final RemoteObjectFactory factory;
    private final LoadedObjects loadedObjects;

	public RemoteOneToOneAssociation(OneToOneAssociation local , final SessionId securityToken, final RemoteObjectFactory factory, final DistributionInterface connection, LoadedObjects loadedObjects) {
		this.local = local;
	   	this.securityToken = securityToken;
    	this.factory = factory;
    	this.connection = connection;
    	this.loadedObjects = loadedObjects;
	}

	public About getAbout(Session session, NakedObject inObject, NakedObject associate) {
		if(inObject.getOid() != null && fullProxy) {
            throw new NotExpectedException();
        } else {
        	return local.getAbout(session, inObject, associate);
        }
    }

    public NakedObject getAssociation(NakedObject inObject) {
        if (isPersistent(inObject) && fullProxy) {
            LOG.debug("remote get association " + inObject);
            ObjectReference targetReference = factory.createObjectReference(inObject);
            ObjectData graph = connection.getAssociation(securityToken, targetReference, getName());
            return graph.recreateObject(loadedObjects, inObject.getContext());
        } else {
        	return local.getAssociation(inObject);
        }
    }

    public void clearAssociation(NakedObject inObject, NakedObject associate) {
        LOG.debug("remote clear association " + inObject + "/" + associate);

        if (isPersistent(inObject)) {
	        try {
                ObjectReference targetReference = factory.createObjectReference(inObject);
                ObjectReference associateReference = factory.createObjectReference((NakedObject) getAssociation(inObject));
	            connection.dissociateObject(securityToken, targetReference, getName(), associateReference);
	        } catch (NakedObjectRuntimeException e) {
	            LOG.error("Distribution problem ", e.getCause());
	            throw (NakedObjectRuntimeException) e.getCause();
	        }
        } else {
        	local.clearAssociation(inObject, associate);
        }
    }

    public void setAssociation(NakedObject inObject, NakedObject associate) {
        LOG.debug("remote set association " + getName() + " in " + inObject + " with " + associate);

        if (isPersistent(inObject)) {
	        try {
                ObjectReference targetReference = factory.createObjectReference(inObject);
                ObjectReference associateReference = factory.createObjectReference((NakedObject) associate);
	            connection.associateObject(securityToken, targetReference, getName(), associateReference);
	        } catch (NakedObjectRuntimeException e) {
	            LOG.error("Distribution problem ", e.getCause());
	            throw (NakedObjectRuntimeException) e.getCause();
	        }
        } else {
        	local.setAssociation(inObject, associate);
        }
    }
    
	public boolean isDerived() {
		return local.isDerived();
	}

	public boolean hasAbout() {
		return local.hasAbout();
	}
	
	public boolean hasAddMethod() {
		return local.hasAddMethod();
	}

	public String getName() {
		return local.getName();
	}

	public NakedObjectSpecification getType() {
		return local.getType();
	}

	public void initData(NakedObject inObject, Object associate) {
        local.initData(inObject, associate);
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