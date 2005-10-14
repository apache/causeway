package org.nakedobjects;
import org.nakedobjects.container.configuration.TestConfiguration;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.ReflectionFactory;
import org.nakedobjects.object.ReflectorFactory;
import org.nakedobjects.object.defaults.IdentityAdapterHashMap;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.AbstractSpecificationLoader;
import org.nakedobjects.object.defaults.ObjectLoaderImpl;
import org.nakedobjects.object.defaults.PojoAdapterHashImpl;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.persistence.defaults.DefaultPersistAlgorithm;
import org.nakedobjects.object.persistence.defaults.LocalObjectManager;
import org.nakedobjects.object.persistence.defaults.SimpleOidGenerator;
import org.nakedobjects.object.persistence.defaults.TransientObjectStore;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.reflect.JavaAdapterFactory;


public class BasicSystem {
    private final NakedObjectSpecificationLoader specificationLoader;
    private final NakedObjectLoader objectLoader;
    private final NakedObjectsClient nakedObjects;
    private final NakedObjectManager objectManager;
    private final ReflectorFactory reflectorFactory;
    private final ReflectionFactory reflectionFactory;

    public BasicSystem() {
        specificationLoader = new AbstractSpecificationLoader();
        
        this.objectLoader = setupObjectLoader();
        this.objectManager = setupObjectManager();
        reflectionFactory = new LocalReflectionFactory();
        reflectorFactory = new JavaAdapterFactory();
        
        nakedObjects = new NakedObjectsClient();
    }

    protected LocalObjectManager setupObjectManager() {
        LocalObjectManager objectManager = new LocalObjectManager();
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
        nakedObjects.setObjectManager(objectManager);        
        nakedObjects.setReflectorFactory(reflectorFactory);
        nakedObjects.setReflectionFactory(reflectionFactory);

        nakedObjects.init();
    }

    public NakedObject createAdapterForTransient(Object associate) {
        return objectLoader.createAdapterForTransient(associate);
    }

    public void shutdown() {
       NakedObjects.shutdown();
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