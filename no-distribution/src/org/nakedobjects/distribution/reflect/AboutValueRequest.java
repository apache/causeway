package org.nakedobjects.distribution.reflect;


import org.nakedobjects.distribution.ObjectRequest;
import org.nakedobjects.distribution.RequestContext;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.reflect.Value;
import org.nakedobjects.object.reflect.ValueIf;
import org.nakedobjects.security.SecurityContext;
import org.nakedobjects.utility.NotImplementedException;


public class AboutValueRequest extends ObjectRequest {
    private final static long serialVersionUID = 1L;
	private SecurityContext context;
    private String fieldName;
    
    public AboutValueRequest(SecurityContext context, NakedObject object, ValueIf field) {
        super(object);
        this.context = context;
        this.fieldName = field.getName();
        
        throw new NotImplementedException("Not updated to carry security context");
    }

    public About about() throws ObjectStoreException {
        sendRequest();
        return (About) response;
    }

    protected void generateResponse(RequestContext server) {
        try {
            NakedObject object = getObject(server.getLoadedObjects());
            Value field = (Value) object.getNakedClass().getField(fieldName);

            response = (field == null) ? null : field.getAbout(context, object);
        } catch (ObjectStoreException e) {
            response = e;
        }
    }

    public String toString() {
        return "About [" + fieldName + "]";
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