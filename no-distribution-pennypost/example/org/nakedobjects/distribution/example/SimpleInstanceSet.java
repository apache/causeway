package org.nakedobjects.distribution.example;

import org.nakedobjects.distribution.InstanceSet;
import org.nakedobjects.distribution.ObjectData;
import org.nakedobjects.distribution.RemoteObjectFactory;
import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.defaults.collection.InstanceCollectionVector;

public class SimpleInstanceSet implements InstanceSet {
    private String className;
    private ObjectData[] elements;

    public SimpleInstanceSet(TypedNakedCollection instances, RemoteObjectFactory factory) {
        className = instances.getElementSpecification().getFullName();
        int size = instances.size();
        elements = new ObjectData[size];
        for (int i = 0; i < size; i++) {
            NakedObject element = instances.elementAt(i);
            elements[i] = factory.createObjectData(element);
        }
    }

    public TypedNakedCollection recreateInstances(LoadedObjects loadedObjects) {
        NakedObject[] instances;
        int size = elements.length;
        instances = new NakedObject[size];
        for (int i = 0; i < size; i++) {
            ObjectData element = elements[i];
            // TODO pass in context
            instances[i] = element.recreateObject(loadedObjects, null);
        }
        
        NakedObjectSpecification cls;
        cls = NakedObjectSpecificationLoader.getInstance().loadSpecification(className);
        return new InstanceCollectionVector(cls, instances);
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