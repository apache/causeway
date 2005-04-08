package org.nakedobjects.example.xat;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.application.NakedObjectRuntimeException;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.NakedObjectSpecificationImpl;
import org.nakedobjects.object.defaults.NakedObjectSpecificationLoaderImpl;
import org.nakedobjects.object.defaults.NullUpdateNotifier;
import org.nakedobjects.object.fixture.FixtureBuilder;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.object.persistence.defaults.LocalObjectManager;
import org.nakedobjects.object.persistence.defaults.TimeBasedOidGenerator;
import org.nakedobjects.object.persistence.defaults.TransientObjectStore;
import org.nakedobjects.object.reflect.PojoAdapterFactory;
import org.nakedobjects.object.reflect.PojoAdapterHashImpl;
import org.nakedobjects.reflector.java.JavaBusinessObjectContainer;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.fixture.JavaFixtureBuilder;
import org.nakedobjects.reflector.java.reflect.JavaReflectorFactory;
import org.nakedobjects.utility.StartupException;
import org.nakedobjects.xat.AcceptanceTestCase;
import org.nakedobjects.xat.ClearableLoadedObjectsHashtable;



public abstract class JavaAcceptanceTestCase extends AcceptanceTestCase {
    private ClearableLoadedObjectsHashtable loadedObjectsHashtable;

    public JavaAcceptanceTestCase(String name) {
        super(name);
    }
    
    protected FixtureBuilder createFixtureBuilder() {
        return new JavaFixtureBuilder();
    }

    protected void setUp() throws Exception {
        super.setUp();
        clearLoadedObjects();
    }
    
    protected final void setupFramework() {
        JavaBusinessObjectContainer container = new JavaBusinessObjectContainer();

        loadedObjectsHashtable = new ClearableLoadedObjectsHashtable();

        JavaObjectFactory objectFactory = new JavaObjectFactory();
        objectFactory.setContainer(container);

        container.setObjectFactory(objectFactory);
        
	  //      XatObjectStore objectStore = new XatObjectStore();
        TransientObjectStore objectStore = new TransientObjectStore();
        objectStore.setLoadedObjects(loadedObjectsHashtable);

        OidGenerator oidGenerator = new TimeBasedOidGenerator();            

        LocalObjectManager objectManager = new LocalObjectManager();
        objectManager.setObjectStore(objectStore);
        objectManager.setNotifier(new NullUpdateNotifier());
        objectManager.setObjectFactory(objectFactory);
        objectManager.setOidGenerator(oidGenerator);
        objectManager.setLoadedObjects(loadedObjectsHashtable);

        NakedObjects.setObjectManager(objectManager);
        
        container.setObjectManger(objectManager);

        NakedObjectSpecificationLoaderImpl specificationLoader = new NakedObjectSpecificationLoaderImpl();

        NakedObjects.setSpecificationLoader(specificationLoader);
        
        LocalReflectionFactory reflectionFactory = new LocalReflectionFactory();

        JavaReflectorFactory reflectorFactory = new JavaReflectorFactory();
        
        PojoAdapterFactory pojoAdapterFactory = new PojoAdapterFactory();
        pojoAdapterFactory.setPojoAdapterHash(new PojoAdapterHashImpl());
        pojoAdapterFactory.setReflectorFactory(reflectorFactory);
        NakedObjects.setPojoAdapterFactory(pojoAdapterFactory);

        NakedObjectSpecificationImpl.setReflectionFactory(reflectionFactory);
        specificationLoader.setReflectorFactory(reflectorFactory);

        reflectorFactory.setObjectFactory(objectFactory);
        
        try {
            objectManager.init();
        } catch (StartupException e) {
            throw new NakedObjectRuntimeException(e);
        }
        
        
        Runtime.getRuntime().gc();
    }
    
    protected void clearLoadedObjects() {
        loadedObjectsHashtable.clear();
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
