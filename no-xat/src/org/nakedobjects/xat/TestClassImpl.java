package org.nakedobjects.xat;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.NotPersistableException;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.reflect.ActionSpecification;
import org.nakedobjects.object.security.Session;


public class TestClassImpl extends AbstractTestObject implements TestClass {

    public static void init(NakedObjectStore objectStore) {}

    private NakedClass nakedClass;

    public TestClassImpl(Session session, NakedClass cls, TestObjectFactory factory) {
        super(session, factory);
         nakedClass = cls;
    }

    /**
     * Finds the instance whose title matched the one specified. A match is any
     * substring matching the specified text, and the result is the first object
     * found that gives such a match, i.e. only one object is returned even
     * though more than one match might occur.
     */
    public TestObject findInstance(String title) {
        NakedObjectContext context = NakedObjectContext.getDefaultContext();
        NakedObjectSpecification type = ((NakedClass) getForObject()).forNakedClass();
        TypedNakedCollection instances = context.getObjectManager().findInstances(type, title, true);
        if (instances.size() == 0) {
            throw new IllegalActionError("No instance found with title " + title);
        } else {
            NakedObject foundObject = instances.elementAt(0);
            return factory.createTestObject(session, foundObject);
        }
    }

    /**
     * Returns the NakedClass that this view represents.
     */
    public final Naked getForObject() {
        return nakedClass;
    }

    public NakedObjectSpecification getSpecification() {
        return nakedClass.getSpecification();
    }
    
    public String getTitle() {
        return nakedClass.titleString().toString();
    }

    /**
     * Get the instances of this class.
     */
    public TestObject instances() {
        NakedObjectContext context = NakedObjectContext.getDefaultContext();
        NakedCollection instances = context.getObjectManager().allInstances(((NakedClass) getForObject()).forNakedClass(), false);
        if (instances.size() == 0) {
            throw new IllegalActionError("Find must find at least one object");
        } else {
            return factory.createTestObject(session, instances);
        }
    }

    /**
     * Creates a new instance of this class.
     */
    public TestObject newInstance() {
        NakedObject object = newInstance(nakedClass);

        return factory.createTestObject(session, object);
    }

    private NakedObject newInstance(NakedClass cls) {
        NakedObject object;

        try {
            object = (NakedObject) cls.forNakedClass().acquireInstance();
            object.setContext(cls.getContext());
            object.getContext().makePersistent(object);

            // NakedObjectManager.getInstance().makePersistent(object);
            // //makePersistent(object);
            object.created();
            object.getContext().getObjectManager().objectChanged(object);
        } catch (NotPersistableException e) {
            object = cls.getContext().getObjectManager().generatorError(
                    "Failed to create instance of " + cls.forNakedClass().getFullName(), e);

            System.out.println("Failed to create instance of " + cls.forNakedClass().getFullName());
            e.printStackTrace();
        }

        return object;
    }

    public void setForObject(Naked object) {
        throw new NakedObjectRuntimeException();
    }
  
    protected ActionSpecification getAction(NakedObjectSpecification nakedClass, String name) {
        return nakedClass.getClassAction(ActionSpecification.USER, name);
    }
    
    protected ActionSpecification getAction(NakedObjectSpecification nakedClass, String name,
            NakedObjectSpecification[] parameterClasses) {
        return nakedClass.getClassAction(ActionSpecification.USER, simpleName(name), parameterClasses);}

    
    /*
    

    public void assertActionExists(final String name) {
        if (getAction(name) == null) {
            throw new NakedAssertionFailedError("Field '" + name + "' is not found in " + getForObject());
        }
    }

    public void assertActionExists(final String name, final TestNaked[] parameters) {
        if (getAction(name, parameters) == null) {
            throw new NakedAssertionFailedError("Field '" + name + "' is not found in " + getForObject());
        }
    }

    public void assertActionExists(final String name, final TestNaked parameter) {
        assertActionExists(name, new TestNaked[] {parameter});
    }

    public void assertActionInvisible(String name) {
        assertActionInvisible(name, getAction(name), new TestNaked[0]);
    }

    private void assertActionInvisible(String name, ActionSpecification action, TestNaked[] parameters) {
        boolean vetoed = action.getAbout(session, (NakedObject) getForObject(), nakedObjects(parameters)).canAccess().isVetoed();
        Assert.assertTrue("action '" + name + "' is visible", vetoed);
    }

    public void assertActionInvisible(String name, TestNaked[] parameters) {
        assertActionInvisible(name, getAction(name, parameters), parameters);
    }

    public void assertActionInvisible(String name, TestNaked parameter) {
        assertActionInvisible(name, new TestNaked[] {parameter});
    }

    public void assertActionUnusable(String name) {
        assertActionUnusable(name, getAction(name), new TestNaked[0]);
    }

    private void assertActionUnusable(String name, ActionSpecification action, TestNaked[] parameters) {
        Naked[] parameterObjects = nakedObjects(parameters);
        boolean vetoed = action.getAbout(session, (NakedObject) getForObject(), parameterObjects).canUse().isVetoed();
        Assert.assertTrue("action '" + name + "' is usable", vetoed);
    }

    public void assertActionUnusable(String name, TestNaked[] parameters) {
        assertActionUnusable(name, getAction(name, parameters), parameters);
    }

    public void assertActionUnusable(String name, TestNaked parameter) {
        assertActionUnusable(name, new TestNaked[] {parameter});
    }

    public void assertActionUsable(String name) {
        assertActionUsable(name, getAction(name), new TestNaked[0]);
    }

    private void assertActionUsable(String name, ActionSpecification action, final TestNaked[] parameters) {
        Naked[] paramaterObjects = nakedObjects(parameters);
        boolean allowed = action.getAbout(session, (NakedObject) getForObject(), paramaterObjects).canUse().isAllowed();
        assertTrue("action '" + name + "' is unusable", allowed);
    }

    public void assertActionUsable(String name, TestNaked[] parameters) {
        assertActionUsable(name, getAction(name, parameters), parameters);
    }

    public void assertActionUsable(String name, TestNaked parameter) {
        assertActionUsable(name, new TestNaked[] {parameter});
    }

    public void assertActionVisible(String name) {
        assertActionVisible(name, getAction(name), new TestNaked[0]);
    }

    private void assertActionVisible(String name, ActionSpecification action, TestNaked[] parameters) {
        boolean allowed = action.getAbout(session, (NakedObject) getForObject(), nakedObjects(parameters)).canAccess()
                .isAllowed();
        assertTrue("action '" + name + "' is invisible", allowed);
    }

    public void assertActionVisible(String name, TestNaked[] parameters) {
        assertActionVisible(name, getAction(name, parameters), parameters);
    }

    public void assertActionVisible(String name, TestNaked parameter) {
        assertActionVisible(name, new TestNaked[] {parameter});
    }

    
    
    
    
    

    public ActionSpecification getAction(final String name) {
        ActionSpecification action = null;

        try {
            NakedObjectSpecification nakedClass = ((NakedObject) getForObject()).getSpecification();
            action = nakedClass.getObjectAction(ActionSpecification.USER, name);
        } catch (NakedObjectSpecificationException e) {
            throw new NakedAssertionFailedError(e.getMessage());
        }
        if (action == null) {
            throw new NakedAssertionFailedError("Method not found: " + name);
        }
        return action;
    }

    public ActionSpecification getAction(final String name, final TestNaked[] parameters) {
        final Naked[] parameterObjects = nakedObjects(parameters);
        final int noParameters = parameters.length;
        final NakedObjectSpecification[] parameterClasses = new NakedObjectSpecification[noParameters];
        for (int i = 0; i < noParameters; i++) {
            parameterClasses[i] = parameterObjects[i].getSpecification();
        }

        try {
            NakedObjectSpecification nakedClass = ((NakedObject) getForObject()).getSpecification();
            ActionSpecification action = nakedClass.getObjectAction(ActionSpecification.USER, simpleName(name), parameterClasses);
            if (action == null) {
                String parameterList = "";
                for (int i = 0; i < noParameters; i++) {
                    parameterList += (i > 0 ? ", " : "") + parameterClasses[i].getFullName();
                }
                throw new NakedAssertionFailedError("Method not found: " + name + "(" + parameterList + ")");
            }
            return action;
        } catch (NakedObjectSpecificationException e) {
            String targetName = getForObject().getSpecification().getShortName();
            String parameterList = "";
            for (int i = 0; i < noParameters; i++) {
                parameterList += (i > 0 ? ", " : "") + parameterClasses[i];
            }
            throw new NakedAssertionFailedError("Can't find action '" + name + "' with parameters (" + parameterList + ") on a "
                    + targetName);
        }
    }
*/
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