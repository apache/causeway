package org.nakedobjects.distribution.reflect;


import org.nakedobjects.distribution.ObjectRequest;
import org.nakedobjects.distribution.RequestContext;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectMemento;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociationIF;


public class GetAssociationRequest extends ObjectRequest {
    private final static long serialVersionUID = 1L;
    private String name;
    
    public GetAssociationRequest(NakedObject object, OneToOneAssociationIF field) {
        super(object);
        name = field.getName();
    }

    protected void generateResponse(RequestContext server) {
        try {
            NakedObject object = getObject(server.getLoadedObjects());
            OneToOneAssociation association = (OneToOneAssociation) object.getNakedClass().getField(name);

            if (association == null) {
                throw new NakedObjectRuntimeException("ObjectAttributeMessage has invalid Field: " + name);
            }
            response = new NakedObjectMemento((NakedObject) association.get(object));
        } catch (ObjectStoreException e) {
            response = e;
        }
    }

    public NakedObject getAssociate() throws ObjectStoreException {
        sendRequest();
        return ((NakedObjectMemento) response).recreateObject(getLoadedObjects());
    }

    public String toString() {
        return "GetAssociationRequest [" + name + "/" + externalOid + "]";
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
