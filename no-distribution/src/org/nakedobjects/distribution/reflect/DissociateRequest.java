package org.nakedobjects.distribution.reflect;


import org.nakedobjects.distribution.ObjectProxy;
import org.nakedobjects.distribution.ObjectRequest;
import org.nakedobjects.distribution.RequestContext;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.reflect.OneToManyAssociationIF;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociationIF;


public class DissociateRequest extends ObjectRequest {
    private final static long serialVersionUID = 1L;
    private String name;
    private ObjectProxy value;

    public DissociateRequest(NakedObject object, OneToManyAssociationIF field, NakedObject associate) {
    	this(object, field.getName(), associate);
    }
    
    public DissociateRequest(NakedObject object, OneToOneAssociationIF field, NakedObject associate) {
    	this(object, field.getName(), associate);
	}

    private DissociateRequest(NakedObject object, String name, NakedObject associate) {
        super(object);
        this.name = name;
        this.value = new ObjectProxy(associate);
    }

    protected void generateResponse(RequestContext server) {
        try {
            NakedObject object = getObject(server.getLoadedObjects());

            // find Field
            OneToOneAssociation att = (OneToOneAssociation) object.getNakedClass().getField(name);

            if (att == null) {
                throw new IllegalStateException("DeleteElementMessage has invalid Field: " + name);
            }
            ((OneToOneAssociation) att).clearAssociation(object, value.recreateObject(server.getLoadedObjects()));
        } catch (ObjectStoreException e) {
            response = e;
        }
    }

    public String toString() {
        return "DissociateRequest [" + name + "/" + value + "]";
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

