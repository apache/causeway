package org.nakedobjects.xat;

import org.nakedobjects.container.configuration.ComponentException;
import org.nakedobjects.container.configuration.ComponentLoader;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationException;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.OidGenerator;
import org.nakedobjects.object.defaults.LocalObjectManager;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.NakedObjectSpecificationImpl;
import org.nakedobjects.object.defaults.NakedObjectSpecificationLoaderImpl;
import org.nakedobjects.object.defaults.NullUpdateNotifier;
import org.nakedobjects.object.defaults.SimpleNakedClass;
import org.nakedobjects.object.defaults.SimpleOidGenerator;
import org.nakedobjects.object.defaults.TransientObjectStore;
import org.nakedobjects.object.exploration.ExplorationFixture;
import org.nakedobjects.object.exploration.ExplorationSetUp;
import org.nakedobjects.object.reflect.defaults.JavaReflectorFactory;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.object.security.Role;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.object.security.User;

import java.util.Hashtable;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public abstract class AcceptanceTestCase extends TestCase {
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

    public TestValue createParameterTestValue(NakedValue value) {
        return testObjectFactory.createParamerTestValue(value);
    }

    protected void setUp() throws Exception {
        LogManager.getLoggerRepository().setThreshold(Level.ERROR);

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

        objectManager = createObjectManager();

        try {
            NakedObjectContext context = new NakedObjectContext(objectManager);
            Session session = ClientSession.getSession();

            explorationSetUp = new ExplorationSetUp(context);
            setUpFixtures();

            if (testObjectFactory == null) {
                Configuration.installConfiguration("xat.properties");
                testObjectFactory = (TestObjectFactory) ComponentLoader.loadComponent("test-object-factory",
                        NonDocumentingTestObjectFactory.class, TestObjectFactory.class);
            }
            documentor = testObjectFactory.getDocumentor();
            documentor.start();
            String className = getClass().getName();
            String methodName = getName().substring(4);
            testObjectFactory.testStarting(className, methodName);

            new NakedObjectSpecificationLoaderImpl();

            explorationSetUp.installFixtures();
            String[] cls = explorationSetUp.getClasses();
            for (int i = 0; i < cls.length; i++) {
                NakedObjectSpecification nc = NakedObjectSpecificationLoader.getInstance().loadSpecification(cls[i]);

                NakedClass spec = new SimpleNakedClass(cls[i]);
                spec.setContext(context);
                spec.setNakedClass(nc);

                TestClass view = testObjectFactory.createTestClass(session, spec);

                classes.put(nc.getFullName().toLowerCase(), view);
            }

            if (session.getUser() == null) {
                User user = new User("exploration user");
                user.setContext(context);
                user.getRoles().add(new Role("explorer"));
                session.setUser(user);
            }
        } catch (Exception e) {
            // If an exception is thrown in setUp then tearDown is not called,
            // hence object manager is left running, but shouldn't be.
            e.printStackTrace();
            objectManager.shutdown();

            throw e;
        }
    }

    protected LocalObjectManager createObjectManager() throws ConfigurationException, ComponentException {
        NakedObjectSpecificationImpl.setReflectionFactory(new LocalReflectionFactory());
        NakedObjectSpecificationImpl.setReflectorFactory(new JavaReflectorFactory());

        NakedObjectStore nos;
        nos = new TransientObjectStore();
        OidGenerator oidGenerator = new SimpleOidGenerator();
        return new LocalObjectManager(nos, new NullUpdateNotifier(), oidGenerator);
    }

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

    public void setTestObjectFactory(TestObjectFactory testObjectFactory) {
        this.testObjectFactory = testObjectFactory;
    }

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
        objectManager.shutdown();
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
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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
