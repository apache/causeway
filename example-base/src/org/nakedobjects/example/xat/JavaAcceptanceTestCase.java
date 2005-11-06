package org.nakedobjects.example.xat;

import org.nakedobjects.object.fixture.FixtureBuilder;
import org.nakedobjects.object.loader.IdentityAdapterHashMap;
import org.nakedobjects.object.loader.ObjectLoaderImpl;
import org.nakedobjects.object.loader.PojoAdapterHashMap;
import org.nakedobjects.object.persistence.DefaultPersistAlgorithm;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.object.persistence.TimeBasedOidGenerator;
import org.nakedobjects.object.persistence.objectstore.ObjectStorePersistor;
import org.nakedobjects.object.persistence.objectstore.inmemory.TransientObjectStore;
import org.nakedobjects.object.reflect.ReflectionPeerFactory;
import org.nakedobjects.object.repository.NakedObjectsClient;
import org.nakedobjects.object.transaction.TransactionPeerFactory;
import org.nakedobjects.reflector.java.JavaBusinessObjectContainer;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.fixture.JavaFixtureBuilder;
import org.nakedobjects.reflector.java.reflect.JavaAdapterFactory;
import org.nakedobjects.reflector.java.reflect.JavaSpecificationLoader;
import org.nakedobjects.utility.Profiler;
import org.nakedobjects.xat.AcceptanceTestCase;

import org.apache.log4j.Logger;



public abstract class JavaAcceptanceTestCase extends AcceptanceTestCase {
    private static final Logger LOG = Logger.getLogger(JavaAcceptanceTestCase.class);
    private static final boolean PROFILER_ON = false;
    private static Profiler classProfiler;
    private Profiler methodProfiler = new Profiler("method");
    

    public JavaAcceptanceTestCase(String name) {
        super(name);
        if(classProfiler == null) {
           classProfiler = new Profiler("class");
        } else {
	        classProfiler.reset();
	        classProfiler.start();
        }
    }
    
    protected FixtureBuilder createFixtureBuilder() {
        return new JavaFixtureBuilder();
    }

    protected void finalize() throws Throwable {
        super.finalize();
        Logger.getLogger(JavaAcceptanceTestCase.class).info("finalizing test case");
    }
    
    protected void setUp() throws Exception {
        LOG.info("test set up " + getName());
        methodProfiler.start();
        super.setUp();
        methodProfiler.stop();
        LOG.info("test set up complete " + getName() + " " + methodProfiler.timeLog());
        if(PROFILER_ON) {System.out.print(getName() + ": \t" + methodProfiler.timeLog());}
    }
    
    protected void tearDown() throws Exception {
        LOG.info("test tear down " + getName());
        methodProfiler.reset();
        methodProfiler.start();
        super.tearDown();
        methodProfiler.stop();
        LOG.info("test tear down complete " + getName() + " " + methodProfiler.timeLog());
        if(PROFILER_ON) {System.out.println(" \t" + methodProfiler.timeLog());}
    }
    
   protected void runTest() throws Throwable {
       LOG.info("test run " + getName());
       methodProfiler.reset();
       methodProfiler.start();
       super.runTest();
       methodProfiler.stop();
       LOG.info("test run complete " + getName() + " " + methodProfiler.timeLog());
       if(PROFILER_ON) {System.out.print(" \t" + methodProfiler.timeLog());}
   }
   
    protected final void setupFramework(NakedObjectsClient nakedObjects) {
        JavaBusinessObjectContainer container = new JavaBusinessObjectContainer();

        JavaObjectFactory objectFactory = new JavaObjectFactory();
        objectFactory.setContainer(container);
        
        TransientObjectStore objectStore = new TransientObjectStore();

        OidGenerator oidGenerator = new TimeBasedOidGenerator();            

        DefaultPersistAlgorithm persistAlgorithm = new DefaultPersistAlgorithm();
        persistAlgorithm.setOidGenerator(oidGenerator);

        ObjectStorePersistor objectManager = new ObjectStorePersistor();
        objectManager.setObjectStore(objectStore);
        objectManager.setPersistAlgorithm(persistAlgorithm);


        nakedObjects.setObjectPersistor(objectManager);


        ReflectionPeerFactory[] factories = new ReflectionPeerFactory[] {
                new TransactionPeerFactory(),
        };
        JavaSpecificationLoader specificationLoader = new JavaSpecificationLoader();
        specificationLoader.setReflectionPeerFactories(factories);

        
        /*        NakedObjectSpecificationLoader specificationLoader;
        specificationLoader = new JavaSpecificationLoader(); 
        /* {
            // TODO this is duplicated in JavaNakedObjectSpecificationLoader
            protected NakedObjectSpecification load(String className) {
                JavaReflector reflector = new JavaReflector(className);
                NakedObjectSpecificationImpl specification = new NakedObjectSpecificationImpl();
                ((NakedObjectSpecificationImpl) specification).reflect(className, reflector);
                return specification;
            }
        };
*/
        //specificationLoader = new JavaSpecificationLoader();
        nakedObjects.setSpecificationLoader(specificationLoader);
        
    //    LocalReflectionFactory reflectionFactory = new LocalReflectionFactory();

        ObjectLoaderImpl objectLoader = new ObjectLoaderImpl();
    	nakedObjects.setObjectLoader(objectLoader);
        objectLoader.setObjectFactory(objectFactory);
        objectLoader.setPojoAdapterMap(new PojoAdapterHashMap());
        objectLoader.setIdentityAdapterMap(new IdentityAdapterHashMap());
        objectLoader.setAdapterFactory(new JavaAdapterFactory());     
        
 //       nakedObjects.setReflectionFactory(reflectionFactory);
        
        objectManager.init();
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
