package org.nakedobjects.distribution.client;


import org.nakedobjects.distribution.Request;
import org.nakedobjects.distribution.RequestContext;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.io.TransferableReader;
import org.nakedobjects.object.io.TransferableWriter;


public class SerialNumberRequest extends Request {
     private String id;

    public SerialNumberRequest(String id) {
        this.id = id;
    }

    public SerialNumberRequest(TransferableReader data) {
        id = data.readString();
    }

    protected void generateResponse(RequestContext server) {
      //  response = new Long(server.getObjectManager().serialNumber(id));
     response.writeLong(server.getObjectManager().serialNumber(id));
    }

    public String getId() {
        return id;
    }

    public long getSerialNumber() throws ObjectStoreException {
	    sendRequest();	
	    return response.readLong();
	    
	//    return ((Long) response).longValue();
	}

    public String toString() {
        return "Serial Number [" + super.id + ",id" + getId() + "]";
    }

    public void writeData(TransferableWriter data) {
        data.writeString(id);
    }
}


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