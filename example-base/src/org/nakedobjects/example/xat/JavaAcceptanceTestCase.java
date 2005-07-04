package org.nakedobjects.example.xat;

import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.application.NakedObjectRuntimeException;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.ObjectLoaderImpl;
import org.nakedobjects.object.defaults.PojoAdapterHashImpl;
import org.nakedobjects.object.fixture.FixtureBuilder;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.object.persistence.defaults.LocalObjectManager;
import org.nakedobjects.object.persistence.defaults.TimeBasedOidGenerator;
import org.nakedobjects.object.persistence.defaults.TransientObjectStore;
import org.nakedobjects.reflector.java.JavaBusinessObjectContainer;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.fixture.JavaFixtureBuilder;
import org.nakedobjects.reflector.java.reflect.JavaReflectorFactory;
import org.nakedobjects.utility.Profiler;
import org.nakedobjects.utility.StartupException;
import org.nakedobjects.xat.AcceptanceTestCase;
import org.nakedobjects.xat.StaticNakedObjectSpecificationLoader;

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

        LocalObjectManager objectManager = new LocalObjectManager();
        objectManager.setObjectStore(objectStore);
        objectManager.setOidGenerator(oidGenerator);

        nakedObjects.setObjectManager(objectManager);

        StaticNakedObjectSpecificationLoader specificationLoader = new StaticNakedObjectSpecificationLoader();

        nakedObjects.setSpecificationLoader(specificationLoader);
        
        LocalReflectionFactory reflectionFactory = new LocalReflectionFactory();

        JavaReflectorFactory reflectorFactory = new JavaReflectorFactory();
        
        ObjectLoaderImpl objectLoader = new ObjectLoaderImpl();
    	nakedObjects.setObjectLoader(objectLoader);
        objectLoader.setObjectFactory(objectFactory);
        objectLoader.setPojoAdapterHash(new PojoAdapterHashImpl());
        objectLoader.setReflectorFactory(reflectorFactory);
        
        nakedObjects.setReflectionFactory(reflectionFactory);
        nakedObjects.setReflectorFactory(reflectorFactory);

        reflectorFactory.setObjectFactory(objectFactory);
        
        try {
            objectManager.init();
        } catch (StartupException e) {
            throw new NakedObjectRuntimeException(e);
        }
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
