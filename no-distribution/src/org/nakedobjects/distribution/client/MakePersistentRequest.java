package org.nakedobjects.distribution.client;


import org.nakedobjects.distribution.Request;
import org.nakedobjects.distribution.RequestContext;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectMemento;

import java.io.Serializable;


public class MakePersistentRequest extends Request {
    private final static long serialVersionUID = 1L;
    private transient NakedObject object;
    private NakedObjectMemento data;
    
    public MakePersistentRequest(NakedObject transientObject) {
        this.object = transientObject;
    	data = new NakedObjectMemento(transientObject);
    }

    protected void generateResponse(RequestContext server) {
        NakedObject object = data.recreateObject(server.getLoadedObjects());
        server.getObjectManager().makePersistent(object);
        response = (Serializable) object.getOid();
    }

    public void makePersistent() {
        sendRequest();
        object.setOid(response);
		object.setResolved();
    }

    public String toString() {
        return "MakePersistent [" + id + ",data=" + data + "]";
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
