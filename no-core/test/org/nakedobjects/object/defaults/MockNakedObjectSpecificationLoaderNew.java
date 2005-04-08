package org.nakedobjects.object.defaults;

import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Hashtable;

public class MockNakedObjectSpecificationLoaderNew extends NakedObjectSpecificationLoader {
    private Hashtable specs = new Hashtable();
    
    public MockNakedObjectSpecificationLoaderNew() {
        super();
    }

    public NakedObjectSpecification loadSpecification(String name) {
        return spec(name);
    }

    public NakedObjectSpecification loadSpecification(Class cls) {
        return spec(cls.getName());
    }

    private NakedObjectSpecification spec(String name) {
        if(specs.containsKey(name)) {
	        NakedObjectSpecification object = (NakedObjectSpecification) specs.get(name);
	        return object;
        } else {
            throw new NakedObjectRuntimeException("No specification registered for " + name);
        }
    }

    public NakedObjectSpecification[] getAllSpecifications() {
        throw new NotImplementedException();
    }

    public void addSpec(String name, NakedObjectSpecification spec) {
        if(specs.containsKey(name)) {
            throw new AssertionError("Can't add spec twice");
        }
        specs.put(name, spec);
    }

    public void addSpec(String name) {
        addSpec(name, new DummyNakedObjectSpecification(name));
    }

    public void shutdown() {}

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