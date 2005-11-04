package test.org.nakedobjects.utility;


import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class ExpectedCallsTest extends TestCase {

    private ExpectedCalls calls;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ExpectedCallsTest.class);
    }

    protected void setUp() throws Exception {
        calls = new ExpectedCalls();

/*        calls.addExpectedMethod("testMethod1");

        calls.addExpectedMethod("testMethod2");
        calls.addExpectedParameter("param1");

        calls.addExpectedMethod("testMethod3");
        calls.addExpectedParameter("param1");
        calls.addExpectedParameter("param2");
        */
        calls.addExpectedMethod("testMethod4");
        calls.addExpectedParameter("param1");
        calls.addExpectedParameter("param2");
        calls.addExpectedParameter("param3");
        
        calls.addExpectedMethod("testMethod1");
        calls.addExpectedParameter("param1");
   }

    public void testNoMethodsCalled() {
        try {
	        calls.verify();
        } catch (AssertionFailedError e) {
            return;
        } 
        fail();
    }

    public void testAllMethodsCalled() {
  /*      calls.addActualMethod("testMethod1");

        calls.addActualMethod("testMethod2");
        calls.addActualParameter("param1");

        calls.addActualMethod("testMethod3");
        calls.addActualParameter("param1");
        calls.addActualParameter("param2");
        */
        calls.addActualMethod("testMethod4");
        calls.addActualParameter("param1");
        calls.addActualParameter("param2");
        calls.addActualParameter("param3");
        
        calls.addActualMethod("testMethod1");
        calls.addActualParameter("param1");
           
        calls.verify();
    }
    
    public void testMethodNameWrong() {
        try {
            calls.addActualMethod("testMethod2");
        } catch (AssertionFailedError e) {
            return;
        } 
        fail();
    }


    public void testMethodParameterWrong() {
        try {
            calls.addActualMethod("testMethod4");
            calls.addActualParameter("param1");
            calls.addActualParameter("param3");
        } catch (AssertionFailedError e) {
            return;
        } 
        fail();
    }

    
    public void testMethodWithTooManyParameters() {
        try {
            calls.addActualMethod("testMethod4");
            calls.addActualParameter("param1");
            calls.addActualParameter("param2");
            calls.addActualParameter("param3");
            calls.addActualParameter("param4");
        } catch (AssertionFailedError e) {
            return;
        } 
        fail();
    }
    


    public void testTooManyMethods() {
        try {
            calls.addActualMethod("testMethod4");
            calls.addActualParameter("param1");
            calls.addActualParameter("param2");
            calls.addActualParameter("param3");
            
            calls.addActualMethod("testMethod1");
            calls.addActualParameter("param1");
            
            calls.addActualMethod("testMethod1");
        } catch (AssertionFailedError e) {
            return;
        } 
        fail();
    }

    public void testTooFewMethods() {
        try {
            calls.addActualMethod("testMethod4");
            calls.addActualParameter("param1");
            calls.addActualParameter("param2");
            calls.addActualParameter("param3");
            
            calls.verify();
        } catch (AssertionFailedError e) {
            return;
        } 
        fail();
    }
    


    public void testMethodWithTooFewParametersWhenNewMethodStarted() {
        
        try {
            calls.addActualMethod("testMethod4");
            calls.addActualParameter("param1");
            calls.addActualParameter("param2");
            
            calls.addActualMethod("testMethod1");
            
        } catch (AssertionFailedError e) {
            return;
        } 
        fail();
    }


    public void testMethodWithTooFewParametersWhenVerifying() {
        
        try {
            calls.addActualMethod("testMethod4");
            calls.addActualParameter("param1");
            calls.addActualParameter("param2");
            calls.addActualParameter("param3");
            
            calls.addActualMethod("testMethod1");
            
            calls.verify();
        } catch (AssertionFailedError e) {
            return;
        } 
        fail();
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