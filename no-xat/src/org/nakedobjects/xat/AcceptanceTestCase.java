package org.nakedobjects.xat;

import org.nakedobjects.container.configuration.ComponentLoader;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationFactory;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.defaults.LocalObjectManager;
import org.nakedobjects.object.exploration.ExplorationFixture;
import org.nakedobjects.object.exploration.ExplorationSetUp;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.object.security.Session;

import java.util.Hashtable;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public abstract class AcceptanceTestCase extends TestCase {
    private static final String TEST_OBJECT_FACTORY = "test-object-factory";
 //  private static final String REFLECTOR_FACTORY = "reflector";
    private Hashtable classes = new Hashtable();
    private Documentor documentor;
    private LocalObjectManager objectManager;
    private TestObjectFactory testObjectFactory;
    private ExplorationSetUp explorationSetUp;

    public AcceptanceTestCase() {}

    public AcceptanceTestCase(String name) {
        super(name);
    }

    protected void append(String text) {
        docln(text);
    }

    private void docln(String string) {
        documentor.docln(string);
    }

    protected TestClass getTestClass(String name) {
        TestClass view = (TestClass) classes.get(name.toLowerCase());

        if (view == null) {
            throw new IllegalArgumentException("Invalid class name " + name);
        } else {
            return view;
        }
    }

    protected void note(String text) {
        docln(text);
    }

    public void addFixture(ExplorationFixture fixture) {
        explorationSetUp.addFixture(fixture);
    }

    public TestValue createParameterTestValue(Object value) {
        return testObjectFactory.createParamerTestValue(value);
    }
    
    public TestNaked createNullParameter(Class cls) {
        return new TestNakedNullParameter(cls);
    }
    
    public TestNaked createNullParameter(String cls) {
        return new TestNakedNullParameter(cls);
    }

    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.ERROR);
        ConfigurationFactory.setConfiguration(new Configuration("xat.properties"));
        PropertyConfigurator.configure(ConfigurationFactory.getConfiguration().getProperties("log4j"));
        
        Logger.getLogger(AcceptanceTestCase.class).debug("XAT Logging enabled - new test: " + getName());

        
        /*
         * TODO Refactor
         * 
         * When tests are run they need to execute against a specific object
         * manager, and create test objects that generate specific output. The
         * following need to be provided to the case: NakedObjectManager
         * TestObjectFactory
         * 
         * These need to be provide differently for different runs of the tests.
         */
        
        /*
        ReflectorFactory reflectorFactory = loadReflector();
        objectManager = createObjectManager(reflectorFactory.getObjectFactory());
 //       reflectorFactory.setObjectManager(objectManager);

 //       objectManager.setFactory(objectFactory);
        
        try {
            explorationSetUp = (ExplorationSetUp) ComponentLoader.loadComponent("exploration-setup", ExplorationSetUp.class);
            NakedObjectContext context = new NakedObjectContext(objectManager);
            explorationSetUp.setContext(context);
            Session session = ClientSession.getSession();


            new NakedObjectSpecificationLoaderImpl();
*/

            if (testObjectFactory == null) {
                 testObjectFactory = (TestObjectFactory) ComponentLoader.loadComponent(TEST_OBJECT_FACTORY,
                        NonDocumentingTestObjectFactory.class, TestObjectFactory.class);
            }
            documentor = testObjectFactory.getDocumentor();
            documentor.start();
            String className = getClass().getName();
            String methodName = getName().substring(4);
            testObjectFactory.testStarting(className, methodName);
        

        explorationSetUp = explorationSetup();
        ClientSession.setSession(new Session());
        Session session = ClientSession.getSession();
        
        setUpFixtures(); 
            
        try{
            explorationSetUp.installFixtures();
            String[] cls = explorationSetUp.getClasses();
            for (int i = 0; i < cls.length; i++) {
                NakedObjectSpecification nc = NakedObjectSpecificationLoader.getInstance().loadSpecification(cls[i]);
                NakedClass spec = new NakedClass(cls[i]);
                TestClass view = testObjectFactory.createTestClass(session, spec);
                classes.put(nc.getFullName().toLowerCase(), view);
            }
        } catch (Exception e) {
            // If an exception is thrown in setUp then tearDown is not called,
            // hence object manager is left running, but shouldn't be.
            e.printStackTrace();
            objectManager.shutdown();
            throw e;
        }
    }

    protected abstract ExplorationSetUp explorationSetup();

    /*
    protected ReflectorFactory loadReflector() throws ConfigurationException, ComponentException {
        NakedObjectSpecificationImpl.setReflectionFactory(new LocalReflectionFactory());
        
        ReflectorFactory reflectorFactory = (ReflectorFactory) ComponentLoader.loadComponent(REFLECTOR_FACTORY, ReflectorFactory.class);
        NakedObjectSpecificationImpl.setReflectorFactory(reflectorFactory);    
        
        return reflectorFactory;
    }
    
    protected LocalObjectManager createObjectManager( ObjectFactory objectFactory) throws ConfigurationException, ComponentException {
        NakedObjectStore nos;
        nos = new TransientObjectStore();
        OidGenerator oidGenerator = new SimpleOidGenerator();
       return new LocalObjectManager(nos, new NullUpdateNotifier(), oidGenerator, objectFactory);
    }
*/
    protected abstract void setUpFixtures();

    protected void startDocumenting() {
        documentor.start();
    }

    /**
     * Marks the start of a new step within a story.
     */
    protected void nextStep() {
        documentor.step("");
    }

    /**
     * Marks the start of a new step within a story. Adds the specified text to
     * the script documentation, which will then be followed by the generated
     * text from the action methods.
     */
    protected void nextStep(String text) {
        documentor.step(text);
    }

    protected void firstStep() {
        startDocumenting();
        nextStep();
    }

    protected void firstStep(String text) {
        startDocumenting();
        nextStep(text);
    }
/*
    public void setTestObjectFactory(TestObjectFactory testObjectFactory) {
        this.testObjectFactory = testObjectFactory;
    }
*/
    protected void stopDocumenting() {
        documentor.stop();
    }

    /**
     * Gives a story a subtitle in the script documentation.
     */
    protected void subtitle(String text) {
        documentor.subtitle(text);
    }

    protected void tearDown() throws Exception {
//        objectManager.shutdown();
        ClientSession.end();
        testObjectFactory.testEnding();
        documentor.stop();
    }

    /**
     * Gives a story a subtitle in the script documentation.
     */
    protected void title(String text) {
        documentor.title(text);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
