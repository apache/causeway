package org.nakedobjects.xat;

import org.nakedobjects.ExplorationClock;
import org.nakedobjects.ExplorationFixture;
import org.nakedobjects.ExplorationSetUp;
import org.nakedobjects.MutableContainer;
import org.nakedobjects.object.LocalObjectManager;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedClassManager;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.NullUpdateNotifier;
import org.nakedobjects.object.TransientObjectStore;
import org.nakedobjects.object.value.Date;
import org.nakedobjects.object.value.Time;
import org.nakedobjects.object.value.TimeStamp;
import org.nakedobjects.security.SecurityContext;
import org.nakedobjects.security.Session;
import org.nakedobjects.security.User;
import org.nakedobjects.utility.ComponentException;
import org.nakedobjects.utility.ConfigurationException;
import org.nakedobjects.utility.NotImplementedException;
import org.nakedobjects.xat.html.HtmlTestObjectFactory;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public abstract class AcceptanceTestCase extends TestCase implements MutableContainer {
    private Hashtable classes = new Hashtable();
    private SecurityContext context;
    private Documentor documentor;
    private ExplorationClock clock;
    private LocalObjectManager objectManager;
    private Vector fixtures = new Vector();
    private TestObjectFactory testObjectFactory;

    public AcceptanceTestCase(String name) {
        super(name);
     }

    /**
     * Register a class as being available to the user.
     * @deprecated
     */
    protected void addClass(Class cls) {
        registerClass(cls.getName());
    }

    protected void append(String text) {
        docln(text);
    }

    private void docln(String string) {
        documentor.docln(string);
    }

    private void flush() {
        documentor.flush();
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

    /** @deprecated */
    public void registerClass(Class cls) {
        registerClass(cls.getName());
    }

    /** @deprecated */
    public void registerClass(String className) {
        NakedClass nc = NakedClassManager.getInstance().getNakedClass(className);
        TestClass view = testObjectFactory.createTestClass(context, nc);

        classes.put(nc.fullName().toLowerCase(), view);
    }
    
    public void addFixture(ExplorationFixture fixture) {
        fixtures.addElement(fixture);
    }

    public TestValue createParameterTestValue(NakedValue value) {
        return testObjectFactory.createParamerTestValue(value);    
    }
    
    protected void setUp() throws Exception {
        LogManager.getLoggerRepository().setThreshold(Level.ERROR);
        clock = new ExplorationClock();
        Date.setClock(clock);
        Time.setClock(clock);
        TimeStamp.setClock(clock);

        
        objectManager = createObjectManager();
        
        try {        
	        setUpFixture();
	        ExplorationSetUp fs = new ExplorationSetUp();
	        fs.init(fixtures, NakedClassManager.getInstance(), objectManager, clock);
	        
	        // TODO replace dynamically
	        if(testObjectFactory == null) {
	            testObjectFactory = new HtmlTestObjectFactory();
	        }
	        
	        documentor =testObjectFactory.getDocumentor(getName().substring(4));
	        
	        String[] cls = fs.getClasses();
	        for (int i = 0; i < cls.length; i++) {
	            NakedClass nc = NakedClassManager.getInstance().getNakedClass(cls[i]);
	            TestClass view = testObjectFactory.createTestClass(context, nc);
	
	            classes.put(nc.fullName().toLowerCase(), view);
	        }
        } catch(Exception e) {
            // If an exception is thrown in setUp the tear is not called, hence object manager is
            // left running, but shouldn't be.
            e.printStackTrace();
            NakedObjectManager.getInstance().shutdown();
            
            throw e;
        }
        
    }
    
    protected LocalObjectManager createObjectManager() throws ConfigurationException, ComponentException {
        NakedObjectStore nos;
        nos = new TransientObjectStore();
        return new LocalObjectManager(nos, new NullUpdateNotifier());
    }

    protected abstract void setUpFixture();

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
        NakedObjectManager.getInstance().shutdown();
        Session.getSession().shutdown();
        documentor.close();
    }

    /**
     * Gives a story a subtitle in the script documentation.
    */
    protected void title(String text) {
        documentor.title(text);
    }

    public ExplorationClock getClock() {
        return clock;
    }

    public NakedObjectManager getObjectManager() {
        return objectManager;
    }

    public void setUser(User user) {
        throw new NotImplementedException();
    }

    public void setTestObjectFactory(TestObjectFactory testObjectFactory) {
        this.testObjectFactory = testObjectFactory;
    }
    
    public NakedClassManager getClassManager() {
        throw new NotImplementedException();
    }

    public Locale getLocale() {
        throw new NotImplementedException();
    }

    public Session getSession() {
        throw new NotImplementedException();
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
