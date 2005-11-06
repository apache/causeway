package org.nakedobjects;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjectPersistor;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.loader.IdentityAdapterHashMap;
import org.nakedobjects.object.loader.ObjectLoaderImpl;
import org.nakedobjects.object.loader.PojoAdapterHashImpl;
import org.nakedobjects.object.persistence.DefaultPersistAlgorithm;
import org.nakedobjects.object.persistence.SimpleOidGenerator;
import org.nakedobjects.object.persistence.objectstore.ObjectStorePersistor;
import org.nakedobjects.object.persistence.objectstore.inmemory.TransientObjectStore;
import org.nakedobjects.object.repository.NakedObjectsClient;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.reflect.JavaAdapterFactory;

import test.org.nakedobjects.object.repository.object.ReflectionFactory;
import test.org.nakedobjects.utility.configuration.TestConfiguration;


public class BasicSystem {
    private final NakedObjectSpecificationLoader specificationLoader;
    private final NakedObjectLoader objectLoader;
    private final NakedObjectsClient nakedObjects;
    private final NakedObjectPersistor objectManager;
    private final ReflectionFactory reflectionFactory;

    public BasicSystem() {
        specificationLoader = new NakedObjectSpecificationLoaderImpl();
        
        this.objectLoader = setupObjectLoader();
        this.objectManager = setupObjectManager();
        reflectionFactory = new LocalReflectionFactory();
        
        nakedObjects = new NakedObjectsClient();
    }

    protected ObjectStorePersistor setupObjectManager() {
        ObjectStorePersistor objectManager = new ObjectStorePersistor();
        DefaultPersistAlgorithm defaultPersistAlgorithm = new DefaultPersistAlgorithm();
        defaultPersistAlgorithm.setOidGenerator(new SimpleOidGenerator());
        objectManager.setPersistAlgorithm(defaultPersistAlgorithm);
        objectManager.setObjectStore(new TransientObjectStore());
        return objectManager;
    }

    protected ObjectLoaderImpl setupObjectLoader() {
        ObjectLoaderImpl objectLoader = new ObjectLoaderImpl();
        objectLoader.setPojoAdapterMap(new PojoAdapterHashImpl());
        objectLoader.setIdentityAdapterMap(new IdentityAdapterHashMap());
        objectLoader.setObjectFactory(new JavaObjectFactory());
        return objectLoader;
    }
    
    public void init() {
         nakedObjects.setConfiguration(new TestConfiguration());
        nakedObjects.setSpecificationLoader(specificationLoader);
        nakedObjects.setObjectLoader(objectLoader);
        nakedObjects.setObjectPersistor(objectManager);        

        nakedObjects.init();
    }

    public NakedObject createAdapterForTransient(Object associate) {
        return objectLoader.createAdapterForTransient(associate);
    }

    public void shutdown() {
       nakedObjects.shutdown();
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user. Copyright (C) 2000 -
 * 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is Kingsway House, 123
 * Goldworth Road, Woking GU21 1NR, UK).
 */