package test.org.nakedobjects.object.reflect;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.defaults.PojoAdapter;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.DummyOid;
import test.org.nakedobjects.object.TestSystem;


public class PojoAdapterTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PojoAdapterTest.class);
    }

    private TestSystem system;

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        system = new TestSystem();
        system.init();
    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    private DummyNakedObjectSpecification setupSpecificationTitleString(final String titleString) {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification(){
            public String getFullName() {
                return TestPojo.class.getName();
            }
        };
        
        spec.setupTitle(titleString);
        system.addSpecification(spec);
        return spec;
    }

    public void testEmptyTitle() {
        setupSpecificationTitleString("");        
        NakedObject pa =   new PojoAdapter(new TestPojo());
        assertEquals("", pa.titleString());
    }

    public void testTitleStringWhereSpecificationProvidesTitleFromObject() {
        setupSpecificationTitleString("object title from specification");

        PojoAdapter pa =   new PojoAdapter(new TestPojo());
        pa.changeState(ResolveState.TRANSIENT);
        pa.persistedAs(new DummyOid(1));
        assertEquals("object title from specification", pa.titleString());
    }

    public void testTitleStringWhereSpecificationReturnNullAsTitle() {
        setupSpecificationTitleString(null);

        PojoAdapter pa =   new PojoAdapter(new TestPojo());
        pa.changeState(ResolveState.TRANSIENT);
        pa.persistedAs(new DummyOid(2));
        assertEquals("A singular name", pa.titleString());
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user. Copyright (C) 2000 -
 * 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is Kingsway House, 123
 * Goldworth Road, Woking GU21 1NR, UK).
 */