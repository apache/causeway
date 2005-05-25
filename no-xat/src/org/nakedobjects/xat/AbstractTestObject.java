package org.nakedobjects.xat;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationException;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NameConvertor;



abstract class AbstractTestObject {
    protected final TestObjectFactory factory;

    public AbstractTestObject(TestObjectFactory factory) {
        this.factory = factory;
    }
    
    public abstract Naked getForNaked();
      
    /**
     Return the title sting from the object this mock is showing
     */
    public abstract String getTitle();
    
    public abstract void setForNaked(Naked object);


    /** @deprecated */
    public TestObject invokeAction(String name) {
        return (TestObject) invokeAction(name, new TestNaked[0]);
    }
    
    /** @deprecated */
    public TestObject invokeAction(String name, TestNaked parameter) {
        return (TestObject) invokeAction(name, new TestNaked[] {parameter} );
   }
  
    public TestObject invokeAction(final String name, final TestNaked[] parameters) {
        return (TestObject) newInvokeAction(name, parameters);
    }
    
    private TestNaked newInvokeAction(final String name, final TestNaked[] parameters) {
        Action action = getAction(simpleName(name), parameters);
        assertActionUsable(name, action, parameters);
        assertActionVisible(name, action, parameters);

        Naked[] parameterObjects = nakedObjects(parameters);
        Naked result = getForNaked().execute(action, parameterObjects);
        if (result == null) {
            return null;
        } else if(result instanceof NakedCollection) {
            return factory.createTestCollection((NakedCollection) result);
        } else {
            return factory.createTestObject((NakedObject) result);
        }

    }

    public TestCollection invokeActionReturnCollection(String name, TestNaked[] parameters) {
        return (TestCollection) newInvokeAction(name, parameters);
    }

    public TestObject invokeActionReturnObject(String name, TestNaked[] parameters) {
        return (TestObject) newInvokeAction(name, parameters);
    }

    
    public String toString() {
        if (getForNaked() == null) {
            return  super.toString() + " " + "null";
        } else {
            return super.toString() + " " + getForNaked().getSpecification().getShortName() + "/" + getForNaked().toString();
        }
    }

    public void assertActionExists(final String name) {
        if (getAction(name) == null) {
            throw new NakedAssertionFailedError("Action '" + name + "' not found in " + getForNaked());
        }
    }

    public void assertActionExists(final String name, final TestNaked[] parameters) {
        if (getAction(name, parameters) == null) {
            throw new NakedAssertionFailedError("Action '" + name + "' not found in " + getForNaked());
        }
    }

    public void assertActionExists(final String name, final TestNaked parameter) {
        assertActionExists(name, new TestNaked[] {parameter});
    }

    public void assertActionInvisible(String name) {
        assertActionInvisible(name, getAction(name), new TestNaked[0]);
    }

    private void assertActionInvisible(String name, Action action, TestNaked[] parameters) {
        Hint hint = getForNaked().getHint(action, nakedObjects(parameters));
        if (hint.canAccess().isAllowed()) {
            throw new NakedAssertionFailedError("Action '" + name + "' is visible");
        }
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
        Consent canUse = getForNaked().getHint(action, parameterObjects).canUse();
        boolean allowed = canUse.isAllowed();
        if (allowed) {
            throw new NakedAssertionFailedError("Action '" + name + "' is usable: " + canUse.getReason());
        }
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
        Consent canUse = getForNaked().getHint(action, paramaterObjects).canUse();
        boolean vetoed = canUse.isVetoed();
        if (vetoed) {
            throw new NakedAssertionFailedError("Action '" + name + "' is unusable: " + canUse.getReason());
        }
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
        boolean allowed = getForNaked().getHint(action, nakedObjects(parameters)).canAccess()
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
            action = getAction(name, new NakedObjectSpecification[0]);
        } catch (NakedObjectSpecificationException e) {
            throw new NakedAssertionFailedError(e.getMessage());
        }
        if (action == null) {
            throw new NakedAssertionFailedError("Method not found: " + name);
        }
        return action;
    }

    public Action getAction(final String name, final TestNaked[] parameters) {
        final Naked[] parameterObjects = nakedObjects(parameters);
        final int noParameters = parameters.length;
        final NakedObjectSpecification[] parameterClasses = new NakedObjectSpecification[noParameters];
        for (int i = 0; i < noParameters; i++) {
            if(parameterObjects[i] == null) {
                parameterClasses[i] = ((TestNakedNullParameter) parameters[i]).getSpecification();
            } else {
                parameterClasses[i] = parameterObjects[i].getSpecification();
            }
        }
    
        try {
            Action action = getAction(name, parameterClasses);
            if (action == null) {
                String parameterList = "";
                for (int i = 0; i < noParameters; i++) {
                    parameterList += (i > 0 ? ", " : "") + parameterClasses[i].getFullName();
                }
                throw new NakedAssertionFailedError("Method not found: " + name + "(" + parameterList + ") in " + getForNaked());
            }
            return action;
        } catch (NakedObjectSpecificationException e) {
            String targetName = getForNaked().getSpecification().getShortName();
            String parameterList = "";
            for (int i = 0; i < noParameters; i++) {
                parameterList += (i > 0 ? ", " : "") + parameterClasses[i];
            }
            throw new NakedAssertionFailedError("Can't find action '" + name + "' with parameters (" + parameterList + ") on a "
                    + targetName);
        }
    }

    protected abstract Action getAction(final String name, final NakedObjectSpecification[] parameterClasses);
       
    protected Naked[] nakedObjects(TestNaked[] parameters) {
        Naked[] parameterObjects = new Naked[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterObjects[i] = (Naked) parameters[i].getForNaked();
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