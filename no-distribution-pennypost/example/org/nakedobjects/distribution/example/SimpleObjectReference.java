package org.nakedobjects.distribution.example;

import org.nakedobjects.distribution.ObjectReference;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.defaults.SerialOid;

public class SimpleObjectReference implements ObjectReference {

    private long id;
    private String className;

    public SimpleObjectReference(NakedObject object) {
        Oid oid = object.getOid();
        className = object.getSpecification().getFullName();
        id = ((SerialOid) oid).getSerialNo();
    }

    public SimpleObjectReference(Oid oid, String className) {
        this.className = className;
        id = ((SerialOid) oid).getSerialNo();
    }

    public NakedObject getObject(NakedObjectManager objectManager) {
        Oid oid = new SerialOid(id);
        NakedObjectSpecification cls = NakedObjectSpecificationLoader.getInstance().loadSpecification(className);
        try {
            return objectManager.getObject(oid, cls);
        } catch (ObjectNotFoundException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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