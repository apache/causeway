package org.nakedobjects.distribution.reflect;

import org.apache.log4j.Category;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.reflect.ActionDelegate;
import org.nakedobjects.object.reflect.Action.Type;
import org.nakedobjects.security.SecurityContext;


public class RemoteAction implements ActionDelegate {
    final static Category LOG = Category.getInstance(RemoteAction.class);
	private boolean fullProxy = false;
	private ActionDelegate local;
	
    public RemoteAction(ActionDelegate local) {
    	this.local = local;
    }

    public NakedObject execute(NakedObject object, NakedObject[] parameters) {
        if (object.isPersistent()) {
	        try {
	            return new ActionRequest(object, this, parameters).executeAction();
	        } catch (ObjectStoreException e) {
	            LOG.error("Problem with distribution", e.getCause());
	
	            return null;
	        }
        } else {
        	return local.execute(object, parameters);
        }
    }
    
     public About getAbout(SecurityContext context, NakedObject object, NakedObject[] parameters) {
        if (object.isPersistent() && fullProxy) {
	     	try {
	            return new AboutActionRequest(object, this, context, parameters).about();
	        } catch (ObjectStoreException e) {
	            LOG.error("Problem with distribution", e.getCause());
	
	            return null;
	        }
        } else {
        	return local.getAbout(context, object, parameters);
        }
    }

	public String getName() {
		return local.getName();
	}

    public int getParameterCount() {
        return local.getParameterCount();
    }

    public Type getType() {
        return local.getType();
    }

	public boolean hasAbout() {
		return local.hasAbout();
	}

	public NakedClass[] parameterTypes() {
		return local.parameterTypes();
	}

	public NakedClass returnType() {
		return local.returnType();
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