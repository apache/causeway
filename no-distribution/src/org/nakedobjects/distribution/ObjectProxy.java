package org.nakedobjects.distribution;

import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedClNakedObjectSpecification;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ResolveException;

import java.io.Serializable;


/**
   To make the passing of the OIDs possible this class combines it with the type of object it is for
   allowing new objects to be created from the OID without further lookup.
   Holds the OID (Object) and object type (String).
 */
public class ObjectProxy implements Serializable {
    private final static long serialVersionUID = 670440236586243634L;
    private final Object oid;
    private final String type;

    public ObjectProxy(String type, Object oid) {
        if (oid == null) {
            throw new IllegalArgumentException(
                "Cannot create an ExternalOid for Naked object that has no OID");
        }

        this.oid = oid;
        this.type = type;
    }

    public ObjectProxy(NakedObject object) {
        this.type = object.getSpecification().getFullName();
        this.oid = object.getOid();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ObjectProxy) {
            ObjectProxy ref = (ObjectProxy) obj;

            return this.oid.equals(ref.oid) && this.type.equals(ref.type);
        }

        return false;
    }

    public int hashCode() {
        int result = 17;

        result = 37 * result + type.hashCode();
        result = 37 * result + oid.hashCode();

        return result;
    }

    /**
       Recreates an object given the oid and type, as stored by this object.
       Instanstiates a new instance of the internally specified type, sets the oid using the internally held oid
       and then set the persistence type to the type specified by the object manager (through the defaultPersistenceType
       method).
       The object is also cached by the object manager.
     */
    public NakedObject recreateObject(LoadedObjects loadedObjects) throws ObjectNotFoundException {
        NakedObject object;
        
        try {
	        NakedObjectManager objectManager = NakedObjectManager.getInstance();
            synchronized(objectManager){
                NakedObjectSpecification cls = NakedObjectSpecification.getNakedClass(type);
                if (loadedObjects.isLoaded(oid)) {
                    object = objectManager.getObject(oid, cls);
                } else {
                    object = (NakedObject) cls.acquireInstance();
                    object.setOid(oid);
                    loadedObjects.loaded(object);
                }
            }
        } catch(ResolveException e) {
			throw new RuntimeException("Object already loaded when trying to load.");
        }

        return object;
    }

    public String toString() {
        return type + "/" + oid;
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