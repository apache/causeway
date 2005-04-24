package org.nakedobjects.utility.xmlsnapshot;

import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationFactory;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.defaults.MockNakedObjectSpecificationLoader;
import org.nakedobjects.object.reflect.DummyNakedObject;
import org.nakedobjects.object.reflect.NakedObjectField;

import junit.framework.TestCase;

import org.w3c.dom.Element;


public class SnapshotBuilderTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SnapshotBuilderTest.class);
    }

    public void testSnapshot() {
        ConfigurationFactory.setConfiguration(new Configuration());
        
      	MockNakedObjectSpecificationLoader loader = new MockNakedObjectSpecificationLoader();
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        loader.addSpec(spec);
        spec.fields = new NakedObjectField[0];
        
 //       TestObject testObject = new TestObject();
        
        DummyNakedObject object = new DummyNakedObject();
        object.setupSpecification(spec);
        XmlSnapshot builder = new XmlSnapshot(object);
        
        Element e = builder.getXmlElement();

//        DomSerializer serializer = new DomSerializerCrimson();
 //       String xml = serializer.serialize(e);

//        assertEquals("", xml);
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