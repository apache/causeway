package org.nakedobjects.object.utility.snapshot;

import org.nakedobjects.LocalReflectionFactory;
import org.nakedobjects.object.loader.ObjectLoaderImpl;
import org.nakedobjects.object.repository.NakedObjectsClient;
import org.nakedobjects.reflector.java.reflect.JavaAdapterFactory;
import org.nakedobjects.utility.configuration.PropertiesConfiguration;
import org.nakedobjects.utility.xmlsnapshot.DomSerializer;
import org.nakedobjects.utility.xmlsnapshot.DomSerializerCrimson;
import org.nakedobjects.utility.xmlsnapshot.XmlSnapshot;

import junit.framework.TestCase;

import org.w3c.dom.Element;

import test.org.nakedobjects.object.repository.object.defaults.AbstractSpecificationLoader;


public class SnapshotBuilderTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SnapshotBuilderTest.class);
    }

    public void testSnapshot() {
        NakedObjectsClient nakedObjects = new NakedObjectsClient();
        nakedObjects.setConfiguration(new PropertiesConfiguration());
        
        new AbstractSpecificationLoader();
        
          nakedObjects.setReflectorFactory(new JavaAdapterFactory());
          nakedObjects.setReflectionFactory(new LocalReflectionFactory());
             
          
          TestObject testObject = new TestObject();
          
          ObjectLoaderImpl objectLoader = new ObjectLoaderImpl();

          XmlSnapshot builder = new XmlSnapshot(objectLoader.createAdapterForTransient(testObject));
          Element e = builder.getXmlElement();

          DomSerializer serializer = new DomSerializerCrimson();
          String xml = serializer.serialize(e);

          assertEquals("", xml);    
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