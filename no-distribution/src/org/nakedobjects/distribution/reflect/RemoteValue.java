package org.nakedobjects.distribution.reflect;


import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.ValueParseException;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Validity;
import org.nakedobjects.object.reflect.ValueIf;
import org.nakedobjects.security.SecurityContext;
import org.nakedobjects.utility.NotImplementedException;

import org.apache.log4j.Category;


public class RemoteValue implements ValueIf {
	private final static Category LOG = Category.getInstance(RemoteValue.class);
	private ValueIf local;
	private boolean fullProxy = false;
	
	public RemoteValue(ValueIf local) {
		this.local = local;
	}
	
    public About getAbout(SecurityContext context, NakedObject inObject) {
    	if(inObject.isPersistent() && fullProxy) {
			try {
				return new AboutValueRequest(context, inObject, this).about();
			} catch (ObjectStoreException e) {
				LOG.error("Problem with distribution", e.getCause());
				return null;
			}
    	} else {
    		return local.getAbout(context, inObject);
    	}
    }

	public void restoreValue(NakedObject inObject, Object setValue) {
		local.restoreValue(inObject, setValue);
	}

	public boolean isDerived() {
		return local.isDerived();
	}

	public Naked get(NakedObject inObject) {
	   	if(inObject.isPersistent() && fullProxy) {
	   		throw new NotImplementedException(); 
	   	} else {
	   		return local.get(inObject);
	   	}
	}

	public boolean hasAbout() {
		return local.hasAbout();
	}

	public String getName() {
		return local.getName();
	}

	public Class getType() {
		return local.getType();
	}

    public void parseValue(NakedValue value, String setValue) throws ValueParseException {
        local.parseValue(value, setValue);
    }

    public void isValid(NakedObject inObject, Validity validity) {
        local.isValid(inObject, validity);
    }

    public void setValue(NakedObject inObject, NakedValue value) {
       	if(inObject.isPersistent()) {
 	       try {
 	            new SetValueRequest(inObject, this, value).execute();
 	        } catch (NakedObjectRuntimeException e) {
 	            LOG.error("Problem with distribution ", e.getCause());
 	            throw (NakedObjectRuntimeException) e.getCause();
 	        }
     	} else {
     		local.setValue(inObject, value);
     	}    
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
