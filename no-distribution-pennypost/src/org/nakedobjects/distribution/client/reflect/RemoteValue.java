package org.nakedobjects.distribution.client.reflect;


import org.nakedobjects.distribution.AboutData;
import org.nakedobjects.distribution.DistributionInterface;
import org.nakedobjects.distribution.ObjectReference;
import org.nakedobjects.distribution.RemoteException;
import org.nakedobjects.distribution.RemoteObjectFactory;
import org.nakedobjects.distribution.SessionId;
import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ValueParseException;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Validity;
import org.nakedobjects.object.reflect.ValueField;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.NotImplementedException;


public final class RemoteValue implements ValueField {
	private ValueField local;
	private boolean fullProxy = false;
	private DistributionInterface connection;
	private SessionId securityToken;
	private RemoteObjectFactory factory;

	public RemoteValue(ValueField local, final SessionId securityToken, final RemoteObjectFactory factory, final DistributionInterface connection) {
		this.local = local;
	   	this.securityToken = securityToken;
    	this.factory = factory;
    	this.connection = connection;
	}
	
    public About getAbout(Session session, NakedObject inObject) {
    	if(inObject.getOid() != null && fullProxy) {
    	    ObjectReference objectReference = factory.createObjectReference(inObject);
    	    AboutData aboutData = connection.aboutValue(securityToken, objectReference, getName());
            return aboutData.recreateAbout();
    	} else {
    		return local.getAbout(session, inObject);
    	}
    }

	public void restoreValue(NakedObject inObject, String encodeValue) {
		local.restoreValue(inObject, encodeValue);
	}

	public boolean isDerived() {
		return local.isDerived();
	}

	public NakedValue getValue(NakedObject inObject) {
	   	if(isPersistent(inObject) && fullProxy) {
	   		throw new NotImplementedException(); 
	   	} else {
	   		return local.getValue(inObject);
	   	}
	}

	public boolean hasAbout() {
		return local.hasAbout();
	}

	public String getName() {
		return local.getName();
	}

	public NakedObjectSpecification getType() {
		return local.getType();
	}

    public void parseValue(NakedValue value, String setValue) throws ValueParseException {
        local.parseValue(value, setValue);
    }

    public void isValid(NakedObject inObject, Validity validity) {
        local.isValid(inObject, validity);
    }

    public void saveValue(NakedObject inObject, String encodedValue) throws InvalidEntryException {
        if(isPersistent(inObject)) {
            try {
                ObjectReference objectReference = factory.createObjectReference(inObject);
                connection.saveValue(securityToken, objectReference, getName(), encodedValue);
            } catch (RemoteException e) {
                throw new InvalidEntryException(e.getMessage());
            }
        } else {
            local.saveValue(inObject, encodedValue);
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
