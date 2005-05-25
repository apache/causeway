package org.nakedobjects.utility;

import java.util.Vector;

import junit.framework.Assert;

public class ExpectedCalls {
    private Vector expectedObjects = new Vector();
    private Vector actualObjects = new Vector();

    private void assertExpectedNoMoreThanActuals() {
        Assert.assertTrue("More actuals than expected; didn't expect call "+ actualObjects.lastElement(), actualObjects.size() <= expectedObjects.size());
    }

    public void verify() {
        assertLastMethodsParametersCorrect();
        Assert.assertTrue("Too few calls added\n  Expected " + expectedObjects, actualObjects.size() == expectedObjects.size());
    }

    private void assertLastMethodsParametersCorrect() {
        int lastActual = actualObjects.size() - 1;
        if(lastActual >= 0) {
            ExpectedCall lastExpectedCall = (ExpectedCall) expectedObjects.elementAt(lastActual);
            ExpectedCall lastActualCall = (ExpectedCall) actualObjects.elementAt(lastActual);
            
            int actualParameterSize = lastActualCall.paramters.size();
            int expectedParameterSize = lastExpectedCall.paramters.size();
            Assert.assertEquals("Method " + lastExpectedCall.name + " parameters incorrect; ", expectedParameterSize, actualParameterSize);
        }
    }

    public void addExpectedMethod(String name) {
        expectedObjects.addElement(new ExpectedCall(name));
    }

    public void addExpectedParameter(Object value) {
        ExpectedCall expected = (ExpectedCall) expectedObjects.lastElement();
        expected.addParameter(value);
    }


    public void addActualMethod(String name) {
        assertLastMethodsParametersCorrect();
        
        ExpectedCall actual = new ExpectedCall(name);
        
        actualObjects.addElement(actual);
        assertExpectedNoMoreThanActuals();
        
        int element = actualObjects.size() - 1;
        
        ExpectedCall expected = (ExpectedCall) expectedObjects.elementAt(element);
        Assert.assertEquals("Actual method does not match expected.\n", 
                expected.name, name);

    }

    public void addActualParameter(Object value) {
        ExpectedCall actual = (ExpectedCall) actualObjects.lastElement();
        actual.addParameter(value);
        
        int expectedElement = actualObjects.size() - 1;
        ExpectedCall expectedCall = (ExpectedCall) expectedObjects.elementAt(expectedElement);
        
        
        int parameterElement = actual.paramters.size() - 1;
        
        int expectedParameterSize = expectedCall.paramters.size();
        if(parameterElement >= expectedParameterSize) {
            Assert.fail("Unexpected number of parameters; expected " + expectedParameterSize + ", but got " + actual.paramters.size());
        }
        
        Object expected = expectedCall.parameterAt(parameterElement);
        Assert.assertEquals("Actual parameter (" + parameterElement + ") in " + expectedCall.name + " does not match expected.\n", 
                expected, value);
    }

}

class ExpectedCall {
    String name;
    Vector paramters = new Vector();
    
    public ExpectedCall(String name) {
        this.name = name;
    }

    public Object parameterAt(int element) {
        return paramters.elementAt(element);
    }

    public void addParameter(Object value) {
        paramters.addElement(value);
    }
    
    public String toString() {
        return name + "(" + paramters + ")";
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