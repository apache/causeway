package org.nakedobjects.object.reflect;

import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.defaults.MockNakedObjectSpecificationLoaderNew;

import java.util.Date;
import java.util.Vector;

import junit.framework.TestCase;


public class ActionParameterSetTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ActionParameterSetTest.class);
    }

    private NakedObjectSpecification[] parameterSpecs;
    private MockNakedObjectSpecificationLoaderNew loader;

    protected void setUp() {
        loader = new MockNakedObjectSpecificationLoaderNew();
        loader.addSpec(Date.class.getName());
        loader.addSpec(String.class.getName());
        loader.addSpec(Vector.class.getName());
        new NakedObjectsClient().setSpecificationLoader(loader);

        parameterSpecs = new NakedObjectSpecification[] { loader.loadSpecification(Date.class),
                loader.loadSpecification(String.class), loader.loadSpecification(Vector.class) };
    }

    public void testMultipleParameters() {
        ActionParameterSet parameters = new ActionParameterSet(new Object[] { new Date(), new String(), new Vector() },
                new String[3], new boolean[3]);
        parameters.checkParameters("methodName", parameterSpecs);
    }

    public void testMultipleParametersWithNoDefault() {
        ActionParameterSet parameters = new ActionParameterSet(new Object[] { new Date(), null, new Vector() },
                new String[3], new boolean[3]);
        parameters.checkParameters("methodName", parameterSpecs);
    }

    public void testMultipleParametersWithWrongType() {
        ActionParameterSet parameters = new ActionParameterSet(new Object[] { new Date(), new Integer(10), new Vector() },
                new String[3], new boolean[3]);
        loader.addSpec(Integer.class.getName());
        try {
            parameters.checkParameters("methodName", parameterSpecs);
            fail();
        } catch (ReflectionException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().startsWith("Parameter 2 in methodName is not of required type;"));
        }
    }

    public void testZeroParameters() {
        NakedObjectSpecification[] parameterSpecs = new NakedObjectSpecification[0];
        ActionParameterSet parameters = new ActionParameterSet(new Object[0], new String[0], new boolean[0]);
        parameters.checkParameters("", parameterSpecs);
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