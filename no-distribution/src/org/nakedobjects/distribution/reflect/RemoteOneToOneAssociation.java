/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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
package org.nakedobjects.distribution.reflect;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.reflect.OneToOneAssociation;

import org.apache.log4j.Category;


public class RemoteOneToOneAssociation implements OneToOneAssociation {
	private final static Category LOG = Category.getInstance(RemoteOneToOneAssociation.class);
	private OneToOneAssociation local;
	private boolean fullProxy = false;
	
	public RemoteOneToOneAssociation(OneToOneAssociation local) {
		this.local = local;
	}

	public About getAbout(SecurityContext context, NakedObject inObject, NakedObject associate) {
		if(inObject.isPersistent() && fullProxy) {
            throw new NotExpectedException();
        } else {
        	return local.getHint(context, inObject, associate);
        }
    }

    public NakedObject getAssociation(NakedObject inObject) {
        if (inObject.isPersistent() && fullProxy) {
            try {
		        LOG.debug("remote get association " + inObject);
                return new GetAssociationRequest(inObject, this).getAssociate();
            } catch (ObjectStoreException e) {
                LOG.error("Problem with distribution", e.getCause());

                return null;
            }
        } else {
        	return local.getAssociation(inObject);
        }
    }

    public void clearAssociation(NakedObject inObject, NakedObject associate) {
        LOG.debug("remote clear association " + inObject + "/" + associate);

        if (inObject.isPersistent()) {
	        try {
	            new DissociateRequest(inObject, this, (NakedObject) getAssociation(inObject)).execute();
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

        if (inObject.isPersistent()) {
	        try {
	            new AssociateRequest(inObject, this, (NakedObject) associate).execute();
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

	public String getName() {
		return local.getName();
	}

	public NakedObjectSpecification getType() {
		return local.getSpecification();
	}

	public void initData(NakedObject inObject, Object associate) {
        local.initData(inObject, associate);
	}
}
