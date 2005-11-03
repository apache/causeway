package test.org.nakedobjects.object.persistence.defaults;

import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.persistence.objectore.inmemory.MemoryObjectStoreInstances;

public class MockMemoryObjectStoreInstances extends MemoryObjectStoreInstances {
    public MockMemoryObjectStoreInstances(NakedObjectLoader objectLoader) {
        super(objectLoader);
    }

    /*
    private PojoAdapterFactory factory;
    
    protected PojoAdapterFactory loaded() {
        return factory;
    }
*/
    public void addElement(Oid oid, Object object, String title) {
        objectInstances.put(oid, object);
        titleIndex.put(title, oid);
    }

    public boolean contains(Oid oid) {
        return objectInstances.containsKey(oid);
    }

    public int size() {
        return objectInstances.size();
    }

   /* public void setLoaded(PojoAdapterFactory factory) {
        this.factory = factory;}
   */ 
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