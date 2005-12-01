package test.org.nakedobjects.object;

import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.utility.NakedObjectRuntimeException;

import java.util.Enumeration;
import java.util.Hashtable;

public class DummyNakedObjectSpecificationLoader implements NakedObjectSpecificationLoader {
    private Hashtable specs = new Hashtable();
    
    public NakedObjectSpecification loadSpecification(String name) {
        if(specs.containsKey(name)) {
            return (NakedObjectSpecification) specs.get(name);
        } else {
            throw new NakedObjectRuntimeException("no specification for " + name);
        }
    }

    public NakedObjectSpecification loadSpecification(Class cls) {
        return loadSpecification(cls.getName());
    }

    public NakedObjectSpecification[] allSpecifications() {
        NakedObjectSpecification[] specsArray;
        specsArray = new NakedObjectSpecification[specs.size()];
        int i = 0;
        Enumeration e = specs.elements();
        while (e.hasMoreElements()) {
            specsArray[i++] =  (NakedObjectSpecification) e.nextElement();
        }
        return specsArray;
    }

    public void shutdown() {}

    public void init() {}

    public void addSpecification(NakedObjectSpecification specification) {
        specs.put(specification.getFullName(), specification);
    }

    public String getDebugData() {
        return null;
    }

    public String getDebugTitle() {
        return null;
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