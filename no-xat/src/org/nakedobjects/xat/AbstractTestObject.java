package org.nakedobjects.xat;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationException;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NameConvertor;
import org.nakedobjects.object.security.Session;

import junit.framework.Assert;


abstract class AbstractTestObject {
    protected final Session session;
    protected final TestObjectFactory factory;

    public AbstractTestObject(Session session, TestObjectFactory factory) {
        this.session = session;
        this.factory = factory;
    }
    
    public abstract NakedObject getForObject();
      
    /**
     Return the title sting from the object this mock is showing
     */
    public abstract String getTitle();
    
    public abstract void setForObject(NakedObject object);

    

    /**
     * Invokes this object's zero-parameter action method of the the given name.
     * This mimicks the right-clicking on an object and subsequent selection of
     * a menu item.
     */
    public TestObject invokeAction(final String name) {
        Action action = getAction(name);
        assertActionUsable(name, action, new TestNaked[0]);
        assertActionVisible(name, action, new TestNaked[0]);

        NakedObject result = getForObject().execute(action, null);
        return ((result == null) ? null : factory.createTestObject(session, result));
    }

    public TestObject invokeAction(final String name, final TestNaked[] parameters) {
        Action action = getAction(simpleName(name), parameters);
        assertActionUsable(name, action, parameters);
        assertActionVisible(name, action, parameters);

        Naked[] parameterObjects = nakedObjects(parameters);
/*        boolean allowed = action.getAbout(session, (NakedObject) getForObject(), parameterObjects).canUse().isAllowed();
        assertTrue("action '" + name + "' is unusable", allowed);

        allowed = action.getAbout(session, (NakedObject) getForObject(), parameterObjects).canAccess().isAllowed();
        assertTrue("action '" + name + "' is invisible", allowed);
*/
        NakedObject result = getForObject().execute(action, parameterObjects);
        if (result == null) {
            return null;
        } else {
            return factory.createTestObject(session, result);
        }

    }

    /**
     * Drop the specified view (object) onto this object and invoke the
     * corresponding <code>action</code> method. A new view representing the
     * returned object, if any is returned, from the invoked <code>action</code>
     * method is returned by this method.
     */
    public TestObject invokeAction(final String name, final TestNaked parameter) {
        return invokeAction(name, new TestNaked[] {parameter});
    }

    public String toString() {
        if (getForObject() == null) {
            return  super.toString() + " " + "null";
        } else {
            return super.toString() + " " + getForObject().getSpecification().getShortName() + "/" + getForObject().toString();
        }
    }

    public void assertActionExists(final String name) {
        if (getAction(name) == null) {
            throw new NakedAssertionFailedError("Action '" + name + "' not found in " + getForObject());
        }
    }

    public void assertActionExists(final String name, final TestNaked[] parameters) {
        if (getAction(name, parameters) == null) {
            throw new NakedAssertionFailedError("Action '" + name + "' not found in " + getForObject());
        }
    }

    public void assertActionExists(final String name, final TestNaked parameter) {
        assertActionExists(name, new TestNaked[] {parameter});
    }

    public void assertActionInvisible(String name) {
        assertActionInvisible(name, getAction(name), new TestNaked[0]);
    }

    private void assertActionInvisible(String name, Action action, TestNaked[] parameters) {
        boolean vetoed = getForObject().getHint(session, action, nakedObjects(parameters)).canAccess().isVetoed();
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

    private void assertActionUnusable(String name, Action action, TestNaked[] parameters) {
        Naked[] parameterObjects = nakedObjects(parameters);
        boolean vetoed = getForObject().getHint(session, action, parameterObjects).canUse().isVetoed();
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

    protected void assertActionUsable(String name, Action action, final TestNaked[] parameters) {
        Naked[] paramaterObjects = nakedObjects(parameters);
        Consent canUse = getForObject().getHint(session, action, paramaterObjects).canUse();
        boolean allowed = canUse.isAllowed();
        assertTrue("action '" + name + "' is unusable: " + canUse.getReason(), allowed);
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

    protected void assertActionVisible(String name, Action action, TestNaked[] parameters) {
        boolean allowed = getForObject().getHint(session, action, nakedObjects(parameters)).canAccess()
                .isAllowed();
        assertTrue("action '" + name + "' is invisible", allowed);
    }

    protected void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new NakedAssertionFailedError(message);
        }
    }

    public void assertActionVisible(String name, TestNaked[] parameters) {
        assertActionVisible(name, getAction(name, parameters), parameters);
    }

    public void assertActionVisible(String name, TestNaked parameter) {
        assertActionVisible(name, new TestNaked[] {parameter});
    }

    public Action getAction(final String name) {
        Action action = null;
    
        try {
            NakedObjectSpecification nakedClass = ((NakedObject) getForObject()).getSpecification();
            action = getAction(nakedClass, name);
        } catch (NakedObjectSpecificationException e) {
            throw new NakedAssertionFailedError(e.getMessage());
        }
        if (action == null) {
            throw new NakedAssertionFailedError("Method not found: " + name);
        }
        return action;
    }

    protected abstract Action getAction(NakedObjectSpecification specification, final String name);

    public Action getAction(final String name, final TestNaked[] parameters) {
        final Naked[] parameterObjects = nakedObjects(parameters);
        final int noParameters = parameters.length;
        final NakedObjectSpecification[] parameterClasses = new NakedObjectSpecification[noParameters];
        for (int i = 0; i < noParameters; i++) {
            parameterClasses[i] = parameterObjects[i].getSpecification();
        }
    
        try {
            NakedObjectSpecification nakedClass = ((NakedObject) getForObject()).getSpecification();
            Action action = getAction(nakedClass, name, parameterClasses);
            if (action == null) {
                String parameterList = "";
                for (int i = 0; i < noParameters; i++) {
                    parameterList += (i > 0 ? ", " : "") + parameterClasses[i].getFullName();
                }
                throw new NakedAssertionFailedError("Method not found: " + name + "(" + parameterList + ") in " + nakedClass.getFullName());
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

    protected abstract Action getAction(NakedObjectSpecification nakedClass, final String name, final NakedObjectSpecification[] parameterClasses);
       
    protected Naked[] nakedObjects(TestNaked[] parameters) {
        Naked[] parameterObjects = new Naked[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterObjects[i] = (Naked) parameters[i].getForObject();
        }
        return parameterObjects;
    }

    protected String simpleName(final String name) {
        return NameConvertor.simpleName(name);
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